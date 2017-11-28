package pacman.utils;

/**
 *
 * @author Texhnolyze
 * @param <T>
 */
public class Stack<T> {
    
    private StackNode<T> top;
    
    private int size;
    
    public Stack() {}
    
    public int size() {
        return size;
    }
    
    public boolean isEmpty() {
        return size == 0;
    }
    
    public void clear() {
        size = 0;
        top = null;
    }
    
    public void push(T elem) {
        size++;
        top = new StackNode<>(elem, top);
    }

    public T top() {
        return top == null ? null : top.elem;
    }
    
    public T nextToTop() {
        return top.prev.elem;
    }
    
    public T pop() {
        size--;
        T elem = top.elem;
        top = top.prev;
        return elem;
    }
    
    @Override
    public String toString() {
        StackNode<T> tmp = top;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size - 1; i++) {
            sb.append(tmp.elem).append(", ");
            tmp = tmp.prev;
        }
        sb.append(tmp.elem);
        return sb.toString();
    }
    
    static class StackNode<T> {
        
        private T elem;
        private StackNode<T> prev;
        
        private StackNode(T elem, StackNode<T> prev) {
            this.elem = elem;
            this.prev = prev;
        }
        
    }
    
}
