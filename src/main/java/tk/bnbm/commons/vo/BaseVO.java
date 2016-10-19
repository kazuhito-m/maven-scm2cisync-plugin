package tk.bnbm.commons.vo;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.io.Serializable;

/**
 * すべてのVOオブジェクトの基底クラス。
 *
 * @author Kazuhito Miura
 */
@SuppressWarnings("serial")
public class BaseVO implements Serializable {

    /**
     * 自身オブジェクトの文字列表現を返す。 プロパティを列挙するようにオーバーライドしている。
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this,
                ToStringStyle.MULTI_LINE_STYLE);
    }
}
