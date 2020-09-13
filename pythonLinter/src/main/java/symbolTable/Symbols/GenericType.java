package symbolTable.Symbols;

public class GenericType extends Symbol implements IType {
    IType genericType;

    public GenericType(String name) {

        super(name);
    }

    public GenericType(String name, IType type){
        super(name);
        this.genericType = type;
    }


    public IType getGenericType(){
        return genericType;
    };

    public void setGenericType(IType type){
        this.genericType = type;
    };

    @Override
    public String toString(){
        return name + ": <" + this.genericType + ">";
    }
}
