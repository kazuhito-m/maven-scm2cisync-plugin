package com.github.kazuhito_m.scm2cisync.core;

import com.github.kazuhito_m.commons.io.StringBuilderInputStream;
import com.github.kazuhito_m.scm2cisync.core.controller.ci.JenkinsControler;
import com.github.kazuhito_m.scm2cisync.core.controller.scm.SubversionControler;
import com.github.kazuhito_m.scm2cisync.core.vo.LastRunInfomation;
import com.github.kazuhito_m.scm2cisync.mojo.SynchronizeTarget;
import org.apache.maven.plugin.logging.Log;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import java.util.*;

/**
 * Javaプロジェクト最適化プラグイン - テスト前設定変更処理 のテスト
 *
 * @author Kazuhito Miura
 */
public class ScmToCiSynchronizer {

    /**
     * デバッグ/情報表示用ログオブジェクト。
     */
    protected Log log = null;

    /**
     * svn-test前の準備処理。
     *
     * @param st 設定値オブジェクト。
     * @throws Exception
     */
    public void synchronize(SynchronizeTarget st) throws Exception {

        LastRunInfomation lastRunInfo = null; // 過去実行情報

        // 設定チェック
        File jobConf = new File(st.getJenkinsJobTemplatePath());
        if (!jobConf.exists()) {
            throw new RuntimeException("not found Jenkins job template file .");
        }

        // 道具の準備

        // SCM(ソース管理システム)コントローラを用意。(現状Subversionのみ)
        SubversionControler scmControler = new SubversionControler();
        scmControler.setLog(log);
        scmControler.setUrlPath(st.getScmUrl());

        scmControler.setAuthType(st.getScmAuthType());
        scmControler.setUserName(st.getScmUserName());
        scmControler.setPassword(st.getScmPassword());

        // CI(継続的インテグレーションサーバ)コントローラを用意。(現状Jenkinsのみ)
        JenkinsControler ciControler = new JenkinsControler();
        ciControler.setLog(log);
        ciControler.setUrlPath(st.getCiUrl());

        // 前の実行ファイルが存在し…設定をvoに取得できたら。
        File lastProfile = new File(st.getLastProfilePath());
        if (lastProfile.exists()) {
            XMLDecoder xmlDec = new XMLDecoder(new BufferedInputStream(
                    new FileInputStream(lastProfile)));
            lastRunInfo = (LastRunInfomation) xmlDec.readObject();
            xmlDec.close();
        }

        // 以前から変更があったか否かを判定。なければ処理をスキップ。
        long headRev = scmControler.getHeadRevisionNo();
        if (lastRunInfo != null) {
            if (lastRunInfo.getScmHeadRevisionNumber() == headRev) {
                log.info("scm not update (rev:" + headRev + "). sync skip.");
                return;
            }
        }

        // SCM(ソース管理システム)にあるか否かを聞く。
        List<String> addedNames = new ArrayList<String>(); // SCMにて追加されたディレクトリ
        List<String> deletedNames = new ArrayList<String>(); // SCMにて削除されたディレクトリ

        // 前回実行ありか、そうでないかで処理を分ける。
        if (lastRunInfo == null) {
            // 最初の一回なら、HEADリビジョンの一覧を取得。
            addedNames = scmControler.getDirNameList();
            lastRunInfo = new LastRunInfomation();
        } else {
            // 前回があれば「前回取得リビジョン以降で追加・削除されたもの」を取得
            long no = lastRunInfo.getScmHeadRevisionNumber();
            addedNames = scmControler.getAddedDirNameList(no);
            deletedNames = scmControler.getDeletedDirNameList(no);
        }
        lastRunInfo.setScmHeadRevisionNumber(headRev); // 最新リビジョンを保存。

        // 設定にある条件でフィルタリング
        addedNames = st.filtering(addedNames);
        Set<String> addedNamesSet = new HashSet<String>(addedNames);

        deletedNames = st.filtering(deletedNames);
        Set<String> deletedNamesSet = new HashSet<String>(deletedNames);

        // CI側のjobの名前を収集(こちらも条件に従いフィルタリング)
        List<String> jobNames = st.filtering(ciControler.getJobNames());
        Set<String> jobNamesSet = new HashSet<String>(jobNames);

        // SCMとCIのマージ処理、開始。
        boolean isModify = false;
        for (String name : addedNamesSet) {
            if (!jobNamesSet.contains(name)) {
                // SCMで追加されたものがCI側に無いなら、新規jobとして追加。
                addCiJob(ciControler, name, st, scmControler);
                isModify = true;
                log.info("ci job '" + name + "' added.");
            }
        }
        // 「SCM中で削除されていたものはjobを無効化する」がOnなら。
        if (st.isJobDisableByScmDelete()) {
            for (String name : deletedNamesSet) {
                if (jobNamesSet.contains(name)) {
                    // SCMで削除されたものがCI側に在るなら、jobを無効化。
                    ciControler.desibleJob(name);
                    isModify = true;
                    log.info("ci job '" + name + "' disabled.");
                }
            }
        }

        // 変更がなされていたら、コントロールjobの「他のプロジェクトのビルド」に現在あるすべてのjobを追加。
        if (isModify) {
            // 変更されたので、再度CI側のjobの名前を収集
            String contJobName = st.getCiControlJobName();
            jobNames = st.filtering(ciControler.getJobNames());
            jobNamesSet = new HashSet<String>(jobNames);
            jobNamesSet.remove(contJobName); // コントロールジョブ名が含まれているなら、削除
            jobNames = new ArrayList<String>(jobNamesSet); // 引数に合わせるため再度リスト化
            // コントロールジョブの下位job一覧を更新。
            ciControler.updateManagingJobNamesForControlJob(contJobName,
                    jobNames);
            log.info("ci control job '" + contJobName + "' update.");
        }

        // 後始末。前回実行情報を保存する。
        if (lastRunInfo != null) {
            XMLEncoder xmlEnc = new XMLEncoder(new BufferedOutputStream(
                    new FileOutputStream(lastProfile)));
            xmlEnc.writeObject(lastRunInfo);
            xmlEnc.close();
        }

    }

    /**
     * 引数に指定されたJob名と各種設定オブジェクトを元にCI側に新規jobを作成する。
     *
     * @param ciControler  CI側コントローラ。
     * @param jobName      job名。
     * @param st           設定値オブジェクト。
     * @param scmControler SCM側コントローラ。(予約、今は扱っていない)
     * @throws Exception すべての例外。
     */
    protected void addCiJob(JenkinsControler ciControler, String jobName,
                            SynchronizeTarget st, SubversionControler scmControler)
            throws Exception {

        // Velocityコンテキストに値を設定
        VelocityContext context = new VelocityContext();
        context.put("jobName", jobName);

        // テンプレートファイルの値を置換、文字列で受け取る。
        File templateFile = new File(st.getJenkinsJobTemplatePath());

        System.out.println("直前のgetJenkinsJobTemplatePath  : " + st.getJenkinsJobTemplatePath());


        String config = getReplaceText(templateFile, context);

        // その文字列を種としてInputStreamを作成、CiControlerを使いjobを新規登録する。
        StringBuilder sb = new StringBuilder(config);
        StringBuilderInputStream sbis = new StringBuilderInputStream(sb);
        // 新規登録、実行。
        ciControler.addJob(jobName, sbis);

        sbis.close();

    }

    /**
     * Velocityを使いファイルの内容を置換後その結果を文字列で返す。
     *
     * @param templateFile 置き換え対象とするテンプレートファイル。
     * @param context      置き換える内容のVelocityContextオブジェクト。
     * @return 置き換えた後の文字列。
     * @throws Exception すべての例外。
     */
    protected String getReplaceText(File templateFile, VelocityContext context)
            throws Exception {

        Velocity.init();

        // テンプレートの読み込み設定。VelocityEngineはClassPath中からファイルを読むように
        Properties props = new Properties();


        System.out.println("templateFile の状況は？  : " + templateFile);


        props.setProperty("file.resource.loader.path", templateFile.getParentFile().getCanonicalPath());
        VelocityEngine ve = new VelocityEngine();
        ve.init(props);

        // テンプレートの作成
        Template template = ve.getTemplate(templateFile.getName(), "utf-8");

        StringWriter sw = new StringWriter();
        template.merge(context, sw);

        String result = sw.toString();
        sw.flush();

        return result;

    }

    /**
     * Logオブジェクトのセット。
     *
     * @param log the log to set
     */
    public void setLog(Log log) {
        this.log = log;
    }

}
