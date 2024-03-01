import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Vector;
import java.util.Iterator;

public class VerificationEngine {

    private final static String VALID_CONDITION_NUM_BOOLEAN = "(true|false|(-?\\d+(\\.\\d+)?))";
    private final static String VALID_CONDITION =
            "((\\w+)|(-?\\d+(\\.\\d+)?))(((\\s?(\\|{2}|\\&{2})\\s?)((\\w+)|(-?\\d+(\\.\\d+)?)))*)";
    private static final String VALID_CALL =
            "\\s*([a-zA-Z]+\\w*)\\s*\\(\\s*(\\s?(.+)\\s*((\\s*,\\s*)(.+))*)*\\s*\\)\\s*;";

    private  static final String CALL_LINE = " *([a-zA-Z]+\\w*) *\\((.*)\\) *;" ;
    private  static  final String RETURN_LINE = " *return *; *";
      private  static  final String IF_WHILE_LINE = "(if|while) +\\((.+)\\) *\\{" ;
    private static final String EMPTY_LINE = "";
    private static final String FINAL_GROUPE = "(final )";
    private static final char SPACE = ' ';
    static final String PRIMITIVE_TYPES_GROUPE = "(int|double|String|char|boolean)";
    private static final String METHOD_DECLARATION = "void +([a-zA-Z]+\\w*) *\\((.*)\\) *\\{" ;
    static final String VAR_NAME_GROUPE = "(_\\w|[a-zA-Z]+\\w*)";
    private static final int FINAL_GROUPE_NUM = 1;
    private static final int TYPE_GROUP_NUM = 2;
    private static final int CHAR_LENGTH = 3;
    private static final String VALUE_GROUP = "([^ ]+)";

    private static final String OPTIONAL_VALUE_GROUP = "(=\\s*" + VALUE_GROUP + "\\s*)*";

    private static final int GLOBAL_SCOPE = 0;
    private static final String ALREADY_INITIALIZED = "tried to initialize already initialized variable";
    private static final String SECOND_VARIABLE_NOT_GOOD_MSG = "second variable is not initialized or not of same type or has no value";
    private static final String VALUE_OF_BAD_TYPE_MSG = "value is not of valid type";
    private static final String ASSIGNMENT_TO_FINAL_VAR = "assignment to final variable";
    private static final String ILLEGAL_SUFFIX_MSG = "illegal suffix";
    private static final String ILLEGAL_COMMENT_OR_ARR_MSG = "illegal comment or array found";
    private static final String INITIALIZING_NON_VAL_FINAL = "initializing a non value final variable";

    private final char[] VALID_SUFFIXES = {'}',';','{'};
    private final char OPEN_SQUARE_BRACKET = '[';
    private final char CLOSE_SQUARE_BRACKET = ']';
    private final FileReader fileReader;
    private final char QUOTA = '"';
    private final char SLASH = '/';
    private final char ASTERISK = '*';
    private final char OPEN_BRACKET = '{';
    private final char Close_BRACKET = '}';
    private final char SEMI_COLON = ';';
    private final HashMap<String, Variable> varSymbolTable;
    private final HashMap<String, MyMethod> functionSymbolTable;
    private int openBrackets;
    private final ArrayList<String> lineArrayListForPass2;
    private final int NO_STRING_NO_COMMENT = 1;
    private final int IN_STRING = 2;
    private final int START_COMMENT = 3;
    private final int IN_COMMENT_TYPE2 = 4;
    private int lineCounter;
    private boolean lastCommandWasRetrun;

    VerificationEngine(FileReader fileReader) {
        this.fileReader = fileReader;
        this.lastCommandWasRetrun = false;
        this.lineArrayListForPass2 = new ArrayList<String>();
        this.varSymbolTable = new HashMap<String, Variable>();
        this.openBrackets = 0;
        this.functionSymbolTable = new HashMap<String, MyMethod>();
    }

    void firstParse() throws IOException, AssignmentException, FinalException, InitializationException, LineSuffixException, IlegalCommentOrArrayException, IllegalLineException {
        //todo check if needs to try open bufferred reader;
        System.out.println(("starting first pass"));
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line;
        int passCount = 1 ;
        while ((line = bufferedReader.readLine()) != null) {
            if(line.isBlank()){
                continue;
            }
            line = replaceMultipleWhiteSpacesToOne(line);
            line = line.trim();// all leading and trailing space removed
            line = removeComment(line);
            if (isIllegalCommentOrArray(line)) {
                throw new IlegalCommentOrArrayException("case# 1: " + ILLEGAL_COMMENT_OR_ARR_MSG + " line: " + line);
            }
            if (line.isBlank()){
                continue;
            }

            // parse and validate function declaration line
            if (isValidFunctionDeclarationLine(line, passCount)) {
                lineArrayListForPass2.add(line);
                continue;
            }

            ValidateLineSuffix(line);

            validateCountEnterExitBlock(line , passCount);

            if (isCloseBracketLine(line)) {
                lineArrayListForPass2.add(line);
                continue;
            }

            // adding internal scope lines to parse two and skip them
            if (this.openBrackets > 0){
                lineArrayListForPass2.add(line);
                continue;
            }

            if ( isValidInstantiationLine(line)) {       //  validate global variable instantiation line
                continue;
            }

            if (isValidAssignmentLine(line, passCount)) { // validate global variable assignment line
                continue;
            }

            // reached illegal line at scope 0
            throw new IllegalLineException("case# 3: " + "illegal line at parse 1." + " line: " + line);


            //else {                                          // for local vars
           //     if(isAssignmentLine(line)){                 // validate line is assignment. if so -
             //       lineArrayListForPass2.add(line);        //         forward it to second pass
            //        continue;
            //    }
             //   else {
             //       throw new IllegalLineException("case# 4: " + "illegal line at parse 1." + " line: " +
            //       line);
            //    }
            //}
        }

        if(this.openBrackets !=0){
            throw new LineSuffixException("case# 5: " + "not all scopes were closed" );
        }

        printVariableTable("end first pass results   --- variables table");
        printFunctionTable("end first pass results   --- function table");
    }


    void secondParse() throws IOException, AssignmentException, FinalException, InitializationException,
            LineSuffixException, IlegalCommentOrArrayException, IllegalLineException {
        //todo check if needs to try open bufferred reader;
        System.out.println("==========================================================");
        System.out.println(("starting second pass"));
        System.out.println(this.lineArrayListForPass2);
        printFunctionTable("");
        String line="";
        int passCount = 2 ;
        this.openBrackets = 0;
        int i;
        int len = lineArrayListForPass2.size();
        for(i =0;i< len; i++)
        {
            SetIsPrevLineReturn(line);

            // read input line
            line = lineArrayListForPass2.get(i);

            // verify function declaration
            if (isValidFunctionDeclarationLine(line , passCount )) {
                continue;
            }

            validateCountEnterExitBlock(line,passCount);

            if (isCloseBracketLine(line)) {
                continue;
            }

            if (isValidStatement(line, passCount)){
                continue;
            }

            throw new IllegalLineException("case# 26: " + "illegal line at pars2" + " line: " + line);

        }
        if(this.openBrackets !=0){
            throw new LineSuffixException("case# 27: " + "not all scopes were closed" + " line: " + line);
        }

        printVariableTable("end of second pass results  ---  variable table");
        printFunctionTable("end of second pass results  ---  variable table");
    }


    private boolean isValidAssignmentLine(String line , int passCount) throws AssignmentException,
            FinalException{
        for (String commaTilComma:line.split(",")) {
            if(!isValidAssignment(commaTilComma , passCount)){
                return false;
            }
        }
        return true;
    }

    private boolean isValidAssignment(String line, int passCount) throws AssignmentException, FinalException{

        // todo - for global variable mark at what scope the variable was assigned, so the assigment can be
        //  removed once exiting the fucntion ( change has_value to assigned_at_scope)
        line = line.substring(0,line.length() - 1);
        Pattern pattern = Pattern.compile(VAR_NAME_GROUPE +" ?= ?"+ VALUE_GROUP);
        Matcher matcher = pattern.matcher(line);
        if(!matcher.matches()){
            return false;
        }
        Variable varTo = bestFit(matcher.group(1),this.openBrackets);
        // defined and not final
        if(varTo == null){
            throw new AssignmentException("case# 6: " + "variable not defined. line: "+line);
        }
        if(varTo.isFinal()){
            throw new FinalException("case# 7: " + "assigning to final" + " line: " + line);
        }

        Variable varFrom = bestFit(matcher.group(2),this.openBrackets);
        if(varFrom != null)
        {
            if (!Variable.isValidTypeEqualsType(varTo.getType(), varFrom.getType()))
            {
                throw new AssignmentException("case# 8: " + VALUE_OF_BAD_TYPE_MSG + "line: " + line);
            }
            if (!varFrom.hasValue())
            {
                throw new AssignmentException("case# 9: " + "other has no value" + "line: " + line);
            }
        }
        else {
            if (!(Variable.isValidValue(varTo.getType(),matcher.group(2))))
            {
                throw new AssignmentException("case# 10: " + "invalid const/string init value" + " line: " + line);
            }
        }

        if ((passCount == 1) && (varTo.getScope()  == 0)) {
            varTo.setHasValue();
        } else if (passCount == 2) {
            varTo.setHasTempValue(true);
        }

        return true;
    }

    private void removeVaribaleFromTable( int scope)
    {
        Vector<String> vars = new Vector<String>();

        // create list of variables at the given scope
        varSymbolTable.entrySet().forEach(entry -> {
            String key = entry.getKey();
            Variable var = varSymbolTable.get(key);
            if (var.getScope() == scope)
            {
                vars.add(key);
            }
            // in case of exit from a function - reset assignments that were done to a global varialbe
            if (scope == 1) {
                if (var.getScope() == 0) {
                    var.setHasTempValue(false);  // reset any assignment done by function to the global variables
                }
            }
        });

        // remove the variables of the given scope
        // Print the vector
        for (int i = 0; i < vars.size(); i++)
        {
            varSymbolTable.remove(vars.get(i));
        }
    }

    private Variable bestFit(String varName,int scope)
    {
        int i = scope;
        while( i >= 0)
        {
            String key = varName+ i;
            if(varSymbolTable.containsKey(key)){
                return varSymbolTable.get(key);}
            i--;

        }
        return null;
    }

// declaration
    private boolean isValidFunctionDeclarationLine(String line , int passCount) throws FinalException,
            InitializationException {
        Pattern pattern = Pattern.compile(METHOD_DECLARATION);
        Matcher matcher = pattern.matcher(line);
        if(!matcher.matches()){
            return false;
        }
        this.openBrackets++;
        String [] args = matcher.group(2).split(",");
        ArrayList<Variable> argsVariableList = new ArrayList<Variable>();

        if ((args.length > 0) || ((args.length == 0) && (args[0] != ""))) {
            Pattern pattern1 =
                    Pattern.compile(" *" + "(final )?" + " *" + PRIMITIVE_TYPES_GROUPE + " +" + VAR_NAME_GROUPE +
                            " *");
            for (String TypeName : args) {
                Matcher matcher1 = pattern1.matcher(TypeName);
                if (!matcher1.matches()) {
                    throw new InitializationException("case# 11: " + "INVALID ARGS DECLARATION" + " line: " + line);
                }
                String g1 = matcher1.group(1);
                String g2 = matcher1.group(2);
                String g3 = matcher1.group(3);

                if (passCount == 1) {
                    argsVariableList.add(new Variable(TypeName.startsWith("final "), g2,
                            g3, true,
                            this.openBrackets));
                } else if (passCount == 2) {
                   // if (varSymbolTable.containsKey())  shuly
                    Variable newVar = new Variable(TypeName.startsWith("final "), g2,
                            g3, true,
                            this.openBrackets);

                    // adding the new variable (argument) to the variable symbol table
                    this.varSymbolTable.put(newVar.getName() + this.openBrackets, newVar);
                }
            }
        }
        if (passCount == 1) {
            String func_name = matcher.group(1).trim();
            if (functionSymbolTable.containsKey(func_name)) {
                throw new InitializationException("case# 12: " + "two functions with the same name" + " line: " + line);
            }
            this.functionSymbolTable.put(matcher.group(1), new MyMethod(matcher.group(1), argsVariableList));
        }

        return true;
    }


    /* this function verify instantiation (declare and assign value) of one variable by constant value
     *  the input string should be at the format  final type name = name, when final is optional.
     * */
    private boolean validTypeNameEqualsValue(String line) throws InitializationException{
        Pattern initializedPattern =
                Pattern.compile(FINAL_GROUPE + "?" +
                        PRIMITIVE_TYPES_GROUPE + "\\s+" +
                        VAR_NAME_GROUPE + "\\s*=\\s*" +
                        VALUE_GROUP + "\\s*");
        Matcher initializedMatcher = initializedPattern.matcher(line);
        //if line is of correct initialized form
        if (initializedMatcher.matches()){

            // verify varTo was not defined at this scope before
            Variable varTo =  varSymbolTable.get(initializedMatcher.group(3)+this.openBrackets);
            if (varTo != null) {
                throw new InitializationException("case# 13: " + ALREADY_INITIALIZED + " line: " + line);
            }

            //not legal value
            if(!(Variable.isValidValue(initializedMatcher.group(2), initializedMatcher.group(4)))){
                throw new InitializationException("case# 14: " + VALUE_OF_BAD_TYPE_MSG + " line: " + line);
            }
            boolean isFinal = initializedMatcher.group(1) != null;
            Variable var = new Variable(isFinal, initializedMatcher.group(2),
                    initializedMatcher.group(3), true, this.openBrackets);
            this.varSymbolTable.put(var.getName()+ this.openBrackets, var);
            return true;
        }
        return false;
    }

    /* this function verify instantiation (declare only) of one variable without intialization
         *  the input string should be at the format  final type name ; when final is optional.
    */
    private boolean validTypeName(String line) throws InitializationException, FinalException {
        Pattern pattern =
                Pattern.compile(FINAL_GROUPE+"?"+PRIMITIVE_TYPES_GROUPE + " +" + VAR_NAME_GROUPE+" *");
        Matcher matcher = pattern.matcher(line);
        //if line is of correct unInitialized form
        if (matcher.matches()) {

            // verify varTo was not defined at this scope before
            //Variable varTo = bestFit(matcher.group(3),this.openBrackets);  // note, guy used group(1)
            Variable varTo =  varSymbolTable.get(matcher.group(3)+this.openBrackets);
            if (varTo != null) {
                throw new InitializationException("case# 15: " + ALREADY_INITIALIZED + " line: " + line);
            }
            if (matcher.group(1)!= null) {
                throw new FinalException("case# 16: " + INITIALIZING_NON_VAL_FINAL + " line: " + line);
            }
            Variable var = new Variable(false, matcher.group(2),
                    matcher.group(3), false, this.openBrackets);
            this.varSymbolTable.put(var.getName()+ this.openBrackets, var);
            return true;
        }
        return false;
    }

    private boolean isValidInstantiationLine(String line) throws InitializationException, FinalException {
        line = line.substring(0,line.length() - 1);
        Pattern pattern = Pattern.compile(FINAL_GROUPE+"?"+PRIMITIVE_TYPES_GROUPE+".*");
        Matcher matcher = pattern.matcher(line);
        if(!matcher.matches()){
            return false;
        }
        String type = matcher.group(2);
        String final_ = "";
        if(matcher.group(1)!= null){
            final_ = matcher.group(1);
        }

        // answer the format of the line must be :   final type varname = value, varname=value....
        boolean isFirstInSplit = true;
        for (String commaTilComma:line.split(",")) {
            if(!isFirstInSplit){
                commaTilComma  = final_+type+" "+commaTilComma;
            }

                /* the following function validate the three different cases of valid initialization value for
                new variable instantiation

                validTypeNameEqualsName  - validate initialization value is a variable; e.g.    int a = b;
                validTypeNameEqualsValue - validate initialization value is constant (string, int, bool).
                                            e.g.  int  a= 8;
                validTypeName            - validate instantiantion without initialization. e.g int x;
                 */

                if(!(validTypeNameEqualsName(commaTilComma) || validTypeNameEqualsValue(commaTilComma) || validTypeName(commaTilComma) )){
                return false;
            }

            isFirstInSplit = false;
        }
        return true;
    }


    /* this function verify instantiation (declare and assign value) of one variable by another variable
    *  the input string should be at the format  final type name = name, when final is optional.
    * */
    private boolean validTypeNameEqualsName(String line) throws InitializationException{
        Pattern pattern =
                Pattern.compile(FINAL_GROUPE+"?"+PRIMITIVE_TYPES_GROUPE + " " + VAR_NAME_GROUPE + " ?= ?"+VAR_NAME_GROUPE);
        Matcher matcher = pattern.matcher(line);
        //if line is of correct unInitialized form
        if (matcher.matches()) {
            if(matcher.group(4).equals("true")||matcher.group(4).equals("false")){
                // initialization by the words 'true' or 'false' is not supported by this function
                return false;
            }

            // verify varTo was not defined at this scope before
            //Variable varTo = bestFit(matcher.group(3),this.openBrackets);
            Variable varTo =  varSymbolTable.get(matcher.group(3)+this.openBrackets);
            if (varTo != null) {
                    throw new InitializationException("case# 17: " + ALREADY_INITIALIZED + " line: " + line);
            }

            // search in the symbol table to find if group(4) is a variable -
            Variable varFrom = bestFit(matcher.group(4),this.openBrackets);
            if (varFrom != null) {
                // throw error in case varFrom type can not be assigned to the new variable
                if (!(Variable.isValidTypeEqualsType(matcher.group(2),varFrom.getType()))) {
                    throw new InitializationException("case# 18: " + SECOND_VARIABLE_NOT_GOOD_MSG + " line: " + line);
                }
                // throw error in cae varFrom was not initialized yet
                if (!varFrom.hasValue()) {
                    throw new InitializationException("case# 19: " + SECOND_VARIABLE_NOT_GOOD_MSG + " line: " + line);
                }
            }
            else {
                // throw error in case varFrom was not declared yet
                    throw new InitializationException("case# 20: " + SECOND_VARIABLE_NOT_GOOD_MSG + " line: " + line);
            }

            // input string is a valid declaration, adding the new variable to
            Variable var = new Variable(matcher.group(1)!=null, matcher.group(2),
                    matcher.group(3), true, this.openBrackets);
            this.varSymbolTable.put(var.getName()+ this.openBrackets, var);
            return true;
        }
        return false;
    }

    private void ValidateLineSuffix(String line) throws LineSuffixException {
        char c = line.charAt(line.length() - 1);
        if(c == Close_BRACKET){
            if(line.length()>1){
                throw new LineSuffixException("case# 21: " + "line of } should be alone" + " line: " + line);
            }
            return;
        }
        else if ((c != OPEN_BRACKET) && (c != SEMI_COLON)){
            throw new LineSuffixException("case# 22: " + ILLEGAL_SUFFIX_MSG + " line: " + line);
        }
    }

    private  boolean  isCloseBracketLine (String line) {
        return (line.charAt(line.length() - 1) == Close_BRACKET);
    }

    private void validateCountEnterExitBlock(String line, int passCount) throws LineSuffixException {
        char c = line.charAt(line.length() - 1);
        if(c == Close_BRACKET){
            if (passCount == 2) {
                if (this.openBrackets == 1) {
                    if (this.lastCommandWasRetrun == false) {
                        throw new LineSuffixException("case# 24: " + "function ends without return command" + " line: " + line);
                    }
                }
            }
            printVariableTable("exit scope  " + this.openBrackets);
            removeVaribaleFromTable( this.openBrackets);
            this.openBrackets--;
        }
        else if(c == OPEN_BRACKET) {
            this.openBrackets++;
        }
    }
    private boolean isIllegalCommentOrArray(String line) {
        int state = NO_STRING_NO_COMMENT;
        for(char c : line.toCharArray()){
            if(state == NO_STRING_NO_COMMENT){
                if (c == QUOTA){
                    state = IN_STRING;
                } else if (c == SLASH) {
                    state = START_COMMENT;
                } else if (c == OPEN_SQUARE_BRACKET || c == CLOSE_SQUARE_BRACKET) {
                    return true;
                }
            } else if (state == IN_STRING) {
                if(c == QUOTA){
                    state = NO_STRING_NO_COMMENT;
                }
            } else if (state == START_COMMENT) {
                if(c == ASTERISK){
                    return true;
                } else if (c == OPEN_SQUARE_BRACKET || c == CLOSE_SQUARE_BRACKET) {
                    return true;
                }else{
                    state = NO_STRING_NO_COMMENT;
                }
            }
        }
        return false;
    }
    private String removeComment(String line) {
        if(line.length() >2 && line.substring(0,2).equals("//")){
            return "";
        }
        return line;
    }
    private String replaceMultipleWhiteSpacesToOne(String line) {
        return line.replaceAll(" +", " ");
    }


    private void SetIsPrevLineReturn(String line) {
        if (isValidReturnStatement(line)) {
            this.lastCommandWasRetrun = true;
        }
        else {
            this.lastCommandWasRetrun = false;
        }
    }

    // todo - itay - type of exception
    private boolean isValidStatement(String line , int passCount) throws AssignmentException, FinalException ,
            InitializationException{

        if (isValidAssignmentLine(line, passCount)) {
            return true;
        } else if (isValidInstantiationLine(line)) {
            return true;
        } else if (isValidIfWhileStatement(line)) {
            return true;
        } else if (isValidCallStatement(line)) {
            return true;
        } else if (isValidReturnStatement(line)) {
            return true;
        } else {
            // todo itay - handle the excpetion -
            throw new InitializationException("case# 29: " + "invalid line at parse 2" + " line: " + line);
        }
    }

    private boolean isValidIfWhileStatement(String line) throws AssignmentException, FinalException ,
            InitializationException {

        Pattern pattern = Pattern.compile(IF_WHILE_LINE);
        Matcher matcher = pattern.matcher(line);
        if(!matcher.matches()){
            return false;
        }

        // validate the condition. in case of error the function validateCondition will throw an exception
        validateCondition(matcher.group(2)) ;

        return true;
    }

    private void validateCondition(String line) throws AssignmentException, FinalException ,
            InitializationException {
        Pattern pattern = Pattern.compile(VALID_CONDITION );
        Matcher matcher = pattern.matcher(line);
        if(!matcher.matches()){
            throw new AssignmentException("case# 30 "+" invalid condition format" + " line: " + line); // itay to handle
            // exceptions
        }

        for (String commaTilComma:line.split("\\|{2}|\\&{2}")) {
            String varName = commaTilComma.trim();
            Pattern patternCond = Pattern.compile(VALID_CONDITION_NUM_BOOLEAN);
            Matcher matcherCond = patternCond.matcher(varName);
            if (!matcherCond.matches()){
                Variable var = bestFit(varName,this.openBrackets);
                if (var == null) {
                    throw new AssignmentException("case# 31 " + " invalid single condition" + " line: " + line);
                    // itay to
                    // handle
                    // exceptions
                }
                if (!var.hasValue()){
                    throw new AssignmentException("case# 32 " + " compare to uninitialized variable" + " line: " + line);
                }
            }
         }
    }

    private boolean isValidCallStatement(String line) throws AssignmentException, FinalException ,
            InitializationException {

        Pattern pattern = Pattern.compile(VALID_CALL);
        Matcher matcher = pattern.matcher(line);
        if(!matcher.matches()){
            return false;
        }

        String func_name = matcher.group(1).trim();
        if ( !(functionSymbolTable.containsKey(func_name))) {
            throw new InitializationException("case# 33: " + " trying to call missing function" + " line: " + line);
        }
        MyMethod func = functionSymbolTable.get(func_name);
        int num_of_args = func.getNumOfArs();
        ArrayList argList = func.getArgList();
        int index = 0;

        // verify the type of the arguments, if any
        String paramList = matcher.group(2);
        if ((paramList==null) && num_of_args !=0) {
            throw new InitializationException("case# 34-1: " + " calling function with missing argument/s" + " line: " + line);
        }

        if (paramList != null) {
            paramList = paramList.trim();
            for (String commaTilComma : paramList.split(",")) {
                if ((index + 1) > num_of_args) {
                    throw new InitializationException("case# 35: " + " trying to send an extra parameter"+ " line: " + line);
                }

                String varName = commaTilComma.trim();
                String callType =  Variable.getTypeConst(varName);
                if (callType == "") { // variable or invalid text
                    Variable var = bestFit(varName, this.openBrackets);
                    if (var == null) {
                        throw new AssignmentException("case# 36 " + " trying to send non-exist variable"+ " line: " + line);
                    }
                    if (!var.hasValue()) {
                        throw new AssignmentException("case# 37 " + " trying to send uninitialized " +
                                "variable" + " line: " + line);
                    }
                    callType = var.getType();
                }

                Variable toVar = (Variable) argList.get(index);
                String toVarType = toVar.getType();

                if (Variable.isValidTypeEqualsType(toVar.getType(), callType) == false) {
                    throw new AssignmentException("case# 38 " + " trying to send incompatible type" + " line: " + line);
                }
                index++;
            }

            if (index < num_of_args) {
                throw new AssignmentException("case# 34-2 " + " calling function with missing argument/s" + " line: " + line);
            }
        }
        return true;
    }

    private boolean isValidReturnStatement(String line)  {
        Pattern pattern = Pattern.compile(RETURN_LINE);
        Matcher matcher = pattern.matcher(line);
        if(!matcher.matches()){
            return false;
        }
        return true;
    }

    private  void printVariableTable_stub(String header) {
        return;
    }
    private  void printVariableTable(String header) {
        System.out.println("\n" + header);
        System.out.println("----------------------------------");
        System.out.println("variable symbol table");
        varSymbolTable.entrySet().forEach(entry -> {
            String key = entry.getKey();
            Variable var = varSymbolTable.get(key);
            String s_final = " ";

            if (var.isFinal()) { s_final = "final ";}
            String s_has_value = "has no value ";

            if (var.hasValue()) { s_has_value = "has_value ";}
            System.out.println(var.getName() + " " + s_final +  " " +var.getType() + " " +  s_has_value + " " + var.getScope());
        });
    }

    private  void printFunctionTable(String header) {

        System.out.println("function symbol table");
        functionSymbolTable.entrySet().forEach(entry -> {
            String key = entry.getKey();
            MyMethod func = functionSymbolTable.get(key);

            System.out.println("----------------------------------");
            System.out.println(func.getName() ) ;
        });
    }


}

/*

    private boolean compileGlobalFunctionParse2(String line) throws FinalException, InitializationException {
        Pattern pattern = Pattern.compile(METHOD_DECLARATION);
        Matcher matcher = pattern.matcher(line);
        if(!matcher.matches()){
            return false;
        }
        this.openBrackets++;
        String [] args = matcher.group(2).split(",");
        //ArrayList<Variable> argsVariableList = new ArrayList<Variable>();

        Pattern pattern1 =
                Pattern.compile(" *"+"(final )?" +" *" + PRIMITIVE_TYPES_GROUPE+" +"+VAR_NAME_GROUPE+
                        " *");
        for (String TypeName:args) {
            Matcher matcher1 = pattern1.matcher(TypeName);
            if(!matcher1.matches()){
                throw new InitializationException("case# 28: " + "INVALID ARGS DECLARATION");
            }
            Variable newVar = new Variable(TypeName.startsWith("final "), matcher1.group(2),
                    matcher1.group(3), true,
                    this.openBrackets);

            // adding the new variable (argument) to the variable symbol table
            this.varSymbolTable.put(newVar.getName()+ this.openBrackets, newVar);
        }
        return true;


 */

//private boolean isLineWhiteSpace(String line) {
//     if(line.equals(EMPTY_LINE) || line.isBlank()){
//        return true;
//    }
//    return false;
//}


//private boolean isAssignmentLine(String line) throws AssignmentException, FinalException {
//    line = line.substring(0,line.length() - 1);
//    Pattern pattern = Pattern.compile(VAR_NAME_GROUPE +" ?= ?"+ VALUE_GROUP);
//    Matcher matcher = pattern.matcher(line);
//    if(!matcher.matches()){
//       return false;
//    }
//    return true;
//}



































