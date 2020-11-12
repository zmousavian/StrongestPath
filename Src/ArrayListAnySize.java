import java.util.ArrayList;


public class ArrayListAnySize<E> extends ArrayList<E>{
    @Override
    public void add(int index, E element){
        if(index >= 0 && index <= size()){
            super.add(index, element);
            return;
        }
        int insertNulls = index - size();
        for(int i = 0; i < insertNulls; i++){
        	super.add(null);
        }
        super.add(element);
    }
}