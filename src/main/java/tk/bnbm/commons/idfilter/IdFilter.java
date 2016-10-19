package tk.bnbm.commons.idfilter;

import tk.bnbm.commons.vo.BaseVO;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 汎用文字列フィルタクラス。ID(正規表現)の規則ごとの設定値を保持する。
 *
 * @author Kazuhito Miura
 */
@SuppressWarnings("serial")
public class IdFilter extends BaseVO {

    /**
     * Patternのflag値の「省略値」
     */
    public static int COMARE_FLAG_OMIT = 0;

    /**
     * 対象にするIDの文字列(正規表現)。
     */
    private ArrayList<String> targetIds = new ArrayList<String>();

    /**
     * 対象外にするIDの文字列(正規表現)。
     */
    private ArrayList<String> excludeIds = new ArrayList<String>();

    /**
     * 指定されたIdがこのパターンに該当するか否か。
     *
     * @param id   アーティファクトID。
     * @param flag 比較フラグ(Patternのflag引数参照)
     * @return 真偽値。該当:true。
     */
    public boolean isAppropriate(final String id, int flag) {
        boolean isHit = false;
        // 絞り込み条件は指定されている。全量検査。
        isHit = isTarget(id, flag);

        // 絞り込み条件で該当判定なら、除外条件を検査
        if (isHit) {
            // 除外条件が設定されている。全量検査。
            if (isExclude(id, flag)) {
                // 正規表現にマッチしたら、フラグを寝かせループ脱出。
                isHit = false;
            }
        }
        return isHit;
    }

    /**
     * 指定されたIdがこのパターンに該当するか否か(比較フラグ省略版)。
     *
     * @param id 対象となるID文字列。
     * @return 真偽値。該当:true。
     */
    public boolean isAppropriate(final String id) {
        return this.isAppropriate(id, COMARE_FLAG_OMIT);
    }

    /**
     * 指定されたIdがtargetのパターンだけに該当するか否か。
     *
     * @param id   対象となるID文字列。
     * @param flag 比較フラグ(Patternのflag引数参照)
     * @return 真偽値。該当:true。
     */
    public boolean isTarget(final String id, int flag) {
        boolean isHit = false;
        if (targetIds != null && targetIds.size() > 0) {
            // 絞り込み条件は指定されている。全量検査。
            for (String regex : targetIds) {
                // 正規表現にマッチしたら、フラグを立てループ脱出。
                if (isHit = isHitRegex(regex, id, flag)) {
                    break;
                }
            }
        } else {
            // 絞り込み条件未指定 ＝ 対象とする
            isHit = true;
        }
        return isHit;
    }

    /**
     * 指定されたIdがtargetのパターンだけに該当するか否か(比較フラグ省略版)。
     *
     * @param id 対象となるID文字列。
     * @return 真偽値。該当:true。
     */
    public boolean isTarget(final String id) {
        return this.isTarget(id, COMARE_FLAG_OMIT);
    }

    /**
     * 指定されたIdがexcludeのパターンだけに該当するか否か。
     *
     * @param id   対象となるID文字列。
     * @param flag 比較フラグ(Patternのflag引数参照)
     * @return 真偽値。該当:true。
     */
    public boolean isExclude(final String id, int flag) {
        boolean isHit = false;
        if (excludeIds != null && excludeIds.size() > 0) {
            // 除外条件は指定されている。全量検査。
            for (String regex : excludeIds) {
                // 正規表現にマッチしたら、フラグを立てループ脱出。
                if (isHit = isHitRegex(regex, id, flag)) {
                    break;
                }
            }
        } else {
            // 除外条件未指定 ＝ AllOKとする
            isHit = false;
        }
        return isHit;
    }

    /**
     * 引数にしていされたIDのリストをフィルタリングする。<br>
     * 元のListオブジェクトの状態を変えてしまうため、注意が必要。
     *
     * @param idList ID文字列を蓄えたListオブジェクト。
     */
    public List<String> filtering(List<String> idList) {
        List<String> result = new ArrayList<String>();
        for (String id : idList) {
            if (this.isAppropriate(id)) {
                result.add(id);
            }
        }
        return result;
    }

    /**
     * 指定されたIdがexcludeのパターンだけに該当するか否か(比較フラグ省略版)。
     *
     * @param id 対象となるID文字列。
     * @return 真偽値。該当:true。
     */
    public boolean isExclude(final String id) {
        return this.isExclude(id, COMARE_FLAG_OMIT);
    }

    /**
     * 自身に設定された条件をすべて削除する。
     */
    public void reset() {
        targetIds.clear();
        excludeIds.clear();
    }

    /**
     * 文字列が正規表現にマッチするか検査する。
     *
     * @param target 検査対象文字列。
     * @param regex  検査する正規表現文字列。
     * @return 結果。該当:true;
     */
    private boolean isHitRegex(String regex, String target, int flag) {
        Pattern pattern = Pattern.compile(regex, flag);
        Matcher matcher = pattern.matcher(target);
        return matcher.find();
    }

    // プロパティ:Setter/Getter

    /**
     * @return the excludeIds
     */
    public ArrayList<String> getExcludeIds() {
        return excludeIds;
    }

    /**
     * @param excludeIds the excludeIds to set
     */
    public void setExcludeIds(ArrayList<String> excludeIds) {
        this.excludeIds = excludeIds;
    }

    /**
     * @return the targetIds
     */
    public ArrayList<String> getTargetIds() {
        return targetIds;
    }

    /**
     * @param targetIds the targetIds to set
     */
    public void setTargetIds(ArrayList<String> targetIds) {
        this.targetIds = targetIds;
    }
}
