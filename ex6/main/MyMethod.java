import java.util.ArrayList;
import java.util.Objects;

public class MyMethod {
    private final String name;
    private final ArrayList<Variable> argList;

    public MyMethod(String name, ArrayList<Variable> argsVariableList) {
        this.name = name;
        this.argList = argsVariableList;
    }

    public ArrayList getArgList() { return this.argList;}
    public int getNumOfArs(){
        return this.argList.size();
    }
    public String getName(){
        return this.name;
    }
    @Override
    public int hashCode() {
        return Objects.hash(name, argList);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null || !(obj instanceof MyMethod)){
            return false;
        }
        MyMethod other = (MyMethod) obj;
        return( other.getName().equals(name) &&
                other.getNumOfArs() == this.getNumOfArs());
    }
}
