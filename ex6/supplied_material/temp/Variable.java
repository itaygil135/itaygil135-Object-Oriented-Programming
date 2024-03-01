import java.awt.*;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Variable {
    private final boolean isFinal;
    private final String type;
    private final String name;
    private final int scopeDepth;
    private boolean hasValue;

    private boolean hasTempValue;
    private static final String CHAR_VALID_PATTERN = "\'.*\'";
    private static final String INT_VALID_PATTERN = "[\\+\\-]?(0*([1-9]\\d*)|(0))";
    private static final String DOUBLE_VALID_PATTERN = "[\\+\\-]?(\\d+\\.\\d*|\\d*\\.\\d+|\\d+)";
    private static final String STRING_VALID_PATTERN = "\".+\"";

    public Variable(boolean isFinal, String type, String varName, boolean hasValue, int scopeDepth) {
        this.isFinal = isFinal;
        this.type = type;
        this.name = varName;
        this.hasValue = hasValue;
        this.hasTempValue = false;
        // todo is scope depth the scope it was initialized/declared in?
        this.scopeDepth = scopeDepth;
    }

    public static  boolean isValidTypeEqualsType(String type1,String type2){
        switch (type1){
            case "int":
                return type2.equals("int");
            case "double":
                return type2.equals("int")||type2.equals("double");
            case "boolean":
                return type2.equals("boolean")|| type2.equals("int")||type2.equals("double");
            case "char":
                return type2.equals("char");
            case "String":
                return type2.equals("String");
            default:
                return false;
        }
    }
    public static  boolean isValidValue(String type, String value) {
        Pattern pattern;
        if(type.equals("char")){
            pattern = Pattern.compile(CHAR_VALID_PATTERN);
        } else if (type.equals("int")) {
            pattern = Pattern.compile(INT_VALID_PATTERN);
        } else if (type.equals("double")) {
            pattern = Pattern.compile(DOUBLE_VALID_PATTERN);
        } else if (type.equals("String")) {
            pattern = Pattern.compile(STRING_VALID_PATTERN);
        } else if (type.equals("boolean")) {
            pattern = Pattern.compile(DOUBLE_VALID_PATTERN+"|true|false");
        }else{
            return false;
        }
        Matcher matcher = pattern.matcher(value);
        return matcher.matches();
    }

    public static String getTypeConst(String value) {

        Pattern pattern;
        Matcher matcher;

        pattern = Pattern.compile(CHAR_VALID_PATTERN);
         matcher = pattern.matcher(value);
        if (matcher.matches()) { return "char"; }

        pattern = Pattern.compile(INT_VALID_PATTERN);
         matcher = pattern.matcher(value);
        if (matcher.matches()) { return "int"; }

        pattern = Pattern.compile(DOUBLE_VALID_PATTERN);
        matcher = pattern.matcher(value);
        if (matcher.matches()) { return  "double"; }

        pattern = Pattern.compile("true|false");
        matcher = pattern.matcher(value);
        if (matcher.matches()) { return "boolean"; }

        pattern = Pattern.compile(STRING_VALID_PATTERN);
        matcher = pattern.matcher(value);
        if (matcher.matches()) { return "String"; }

        return "";
    }

    public int getScope(){ return this.scopeDepth; }
    public String getName(){
        return this.name;
    }

    public String getType() {
        return this.type;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public boolean hasValue() {
        if (this.scopeDepth == 0) {
            return this.hasValue || this.hasTempValue;
        }
        else return this.hasValue;
    }

    public void setHasValue() {
        this.hasValue = true;
    }

    public void setHasTempValue(boolean value) {
        this.hasTempValue = value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, isFinal, type, hasValue);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null || !(obj instanceof Variable)){
            return false;
        }
        Variable other = (Variable) obj;
        return(hasValue == other.hasValue && other.isFinal() == isFinal && other.getName().equals(name) &&
                other.scopeDepth == scopeDepth);
    }
}
