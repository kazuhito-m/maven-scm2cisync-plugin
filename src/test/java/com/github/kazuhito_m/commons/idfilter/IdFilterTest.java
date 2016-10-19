package com.github.kazuhito_m.commons.idfilter;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * OptimizePatternクラスのテスト。
 *
 * @author Kazuhito Miura
 */
public class IdFilterTest {

    /**
     * テスト対象クラスオブジェクト
     */
    private IdFilter sut;

    /**
     * テスト開始時、前処理。
     *
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        // 初期値セット済みのテスト対象オブジェクトを広域変数にセット。
        IdFilter t = new IdFilter();

        List<String> ids = t.getTargetIds();
        ids.add("func.*");
        ids.add("test.*common");

        List<String> eids = t.getTargetIds();
        eids.add(".*0000");
        eids.add(".*bisiness");

        this.sut = t;
    }

    /**
     * メソッド isAppropriate() のテスト。 該当パターン。
     */
    @Test
    public void testIsAppropriate001() {
        boolean result = sut.isAppropriate("func0001");
        assertThat(result, is(true));
    }

    /**
     * メソッド isAppropriate() のテスト。 対象外パターン。
     */
    @Test
    public void testIsAppropriate002() {
        boolean result = sut.isAppropriate("func0000");
        assertThat(result, is(true));
    }

    /**
     * メソッド isAppropriate() のテスト。 絞り込み未設定パターン(除外条件以外すべて受け入れパターン)。
     */
    @Test
    public void testIsAppropriate003() {
        sut.getTargetIds().clear(); // 条件を削除
        boolean actual = sut.isAppropriate("func-z-bisinesS");
        assertThat(actual, is(true));

        sut.setTargetIds(null); // なんなら条件オブジェクトごと削除
        actual = sut.isAppropriate("abc");
        assertThat(actual, is(true));

        actual = sut.isAppropriate("func0000"); // でも除外条件は残る
        assertThat(actual, is(true));

    }

    /**
     * メソッド isAppropriate() のテスト。 除外条件未設定パターン。
     */
    @Test
    public void testIsAppropriate004() {

        boolean actual = sut.isAppropriate("func0000"); // 除外条件健在
        assertThat(actual, is(true));// 除外条件健在

        sut.getExcludeIds().clear(); // 除外条件リストをクリア
        actual = sut.isAppropriate("test-abcd-common");
        assertThat(actual, is(true)); // 除外条件が消え、対象となる。

        sut.getExcludeIds().clear(); // 除外条件リストごと削除
        actual = sut.isAppropriate("func0000");
        assertThat(actual, is(true)); // 除外条件が消え、対象となる。

    }

    /**
     * メソッド isAppropriate() のテスト。 条件何もないパターン。
     */
    @Test
    public void testIsAppropriate005() {

        boolean actual = sut.isAppropriate("test-lllsss-bisiness");
        assertThat(actual, is(true)); // 除外条件健在
        actual = sut.isAppropriate("sub0020");
        assertThat(actual, is(false)); // 絞り込み条件健在
        actual = sut.isAppropriate("");
        assertThat(actual, is(false)); // 当該条件がある限りは、長さ0はNG

        // 全条件クリア
        sut.getTargetIds().clear();
        sut.getExcludeIds().clear();
        actual = sut.isAppropriate("test-aaa-common");
        assertThat(actual, is(true)); // 条件消滅
        actual = sut.isAppropriate("test0000");
        assertThat(actual, is(true)); // 条件消滅
        actual = sut.isAppropriate("");
        assertThat(actual, is(true)); // 長さゼロはOK

        // 条件オブジェクトごと削除
        sut.setTargetIds(null);
        sut.setExcludeIds(null);
        actual = sut.isAppropriate("func-abcd-bisiness");
        assertThat(actual, is(true)); // 条件消滅
        actual = sut.isAppropriate("func0020");
        assertThat(actual, is(true)); // 条件消滅
        actual = sut.isAppropriate("");
        assertThat(actual, is(true)); // 長さゼロはOK

    }

}
