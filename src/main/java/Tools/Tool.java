package Tools;

public class Tool {
    public static String removeCharAt(String s, int pos)
    {
        return s.substring(0, pos) + s.substring(pos + 1);// 使用substring()方法截取0-pos之间的字符串+pos之后的字符串，相当于将要把要删除的字符串删除
    }
}
