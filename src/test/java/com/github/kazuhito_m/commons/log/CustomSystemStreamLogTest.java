package com.github.kazuhito_m.commons.log;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.codehaus.plexus.logging.Logger.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * maven-plugin専用のログクラスのテスト。
 *
 * @author Kazuhito Miura
 */
public class CustomSystemStreamLogTest {

    private CustomSystemStreamLog sut;

    @Before
    public void setUp() throws Exception {
        // 初期値セット済みのテスト対象オブジェクトを広域変数にセット。
        this.sut = new CustomSystemStreamLog();
    }

    @Test
    public void 履歴機能が正しく働いているか_javaのDynamicProxy装備がうまく働くか() throws IOException {
        // 履歴がきちんと設定されるかのテスト
        sut.setDebugEnabled(true);
        sut.setHistoryOn(true);

        sut.info("INFO");
        sut.fatalError("FATAL_ERROR");
        sut.warn("WARN");
        sut.debug("DEBUG");
        sut.error("ERROR");

        List<LogDetail> history = sut.getHistory();

        // 要素の個数の確認。
        assertThat(history.size(), is(5));

        // 要素の順番と値の確認。
        int i = 0;
        for (LogDetail ld : history) {
            switch (i) {
                case 0:
                    assertThat(LEVEL_INFO, is(ld.getLevel()));
                    assertThat("INFO", is(ld.getMessage()));
                    assertThat(null, is(ld.getThrowItem()));
                    break;
                case 1:
                    assertThat(LEVEL_FATAL, is(ld.getLevel()));
                    assertThat("FATAL_ERROR", is(ld.getMessage()));
                    assertThat(null, is(ld.getThrowItem()));
                    break;
                case 2:
                    assertThat(LEVEL_WARN, is(ld.getLevel()));
                    assertThat("WARN", is(ld.getMessage()));
                    assertThat(null, is(ld.getThrowItem()));
                    break;
                case 3:
                    assertThat(LEVEL_DEBUG, is(ld.getLevel()));
                    assertThat("DEBUG", is(ld.getMessage()));
                    assertThat(null, is(ld.getThrowItem()));
                    break;
                case 4:
                    assertThat(LEVEL_ERROR, is(ld.getLevel()));
                    assertThat("ERROR", is(ld.getMessage()));
                    assertThat(null, is(ld.getThrowItem()));
                    break;
                default:
                    break;
            }
            i++;
        }
    }

    public void 履歴機能で絞り込みが正しく行えるか() throws IOException {
        // 履歴がきちんと設定されるかのテスト
        sut.setDebugEnabled(true);
        sut.setHistoryOn(true);

        sut.info("INFO01");
        sut.info("INFO02");
        sut.info("INFO03");
        sut.fatalError("FATAL_ERROR01");
        sut.fatalError("FATAL_ERROR02");
        sut.fatalError("FATAL_ERROR03");
        sut.fatalError("FATAL_ERROR04");
        sut.warn("WARN01");
        sut.warn("WARN02");
        sut.warn("WARN03");
        sut.warn("WARN04");
        sut.warn("WARN05");
        sut.debug("デバッグが");
        sut.debug("キモなので");
        sut.debug("解りやすく");
        sut.debug("書いておこうと");
        sut.debug("思います。");
        sut.debug("アーメン");
        sut.error("ERROR01");
        sut.error("ERROR02");
        sut.error("ERROR03");

        // 絞りこみ
        List<LogDetail> history = sut.getHistory(LEVEL_FATAL, LEVEL_ERROR, LEVEL_DEBUG);

        // 要素の個数の確認。
        assertThat(13, is(history.size()));

        // 要素の順番と値の確認。
        int i = 0;
        for (LogDetail ld : history) {
            switch (i) {
                case 0:
                    assertThat(LEVEL_FATAL, is(ld.getLevel()));
                    assertThat("FATAL_ERROR01", is(ld.getMessage()));
                    assertThat(null, is(ld.getThrowItem()));
                    break;
                case 4:
                    assertThat(LEVEL_DEBUG, is(ld.getLevel()));
                    assertThat("デバッグが", is(ld.getMessage()));
                    assertThat(null, is(ld.getThrowItem()));
                    break;
                case 12:
                    assertThat(LEVEL_ERROR, is(ld.getLevel()));
                    assertThat("ERROR03", is(ld.getMessage()));
                    assertThat(null, is(ld.getThrowItem()));
                    break;
            }
            i++;
        }

    }

}
