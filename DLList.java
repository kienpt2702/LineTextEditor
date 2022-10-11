public class DLList<T> {
    private class DLListNode<T> {
        //data members
        public T data;
        public DLListNode<T> previous;
        public DLListNode<T> next;

        //overloaded constructor
        DLListNode(T value) {
            data = value;
            previous = null;
            next = null;
        }
    }
    private DLListNode<T> front;
    private DLListNode<T> back;
    private DLListNode<T> current;
    private int size;
    private int index;

    public DLList() {
        clear();
    }

    //copy constructor (deep copy -> two separete lists with the exact same info in the,, simply seperate)
    public DLList(DLList<T> other) {
        this();

        DLListNode<T> otherCur = other.front;
        while (otherCur != null) {
            insertLast(otherCur.data);
            otherCur = otherCur.next;
        }
    }

    //clear list method, its purpose is to set front to null, back to null, current to null, size to 0, and index to -1
    public void clear() {
        front = null;
        back = null;
        current = null;
        size = 0;
        index = -1;
    }

    //get size method
    public int getSize() {
        return size;
    }

    //get index method
    public int getIndex() {
        return index;
    }

    //is empty method
    public boolean isEmpty() {
        return getSize() == 0;
    }

    public boolean atFirst() {
        return getIndex() == 0;
    }

    public boolean atLast() {
        return (getIndex() == getSize() - 1);
    }

    //get current node data
    public T getData() {
        return !isEmpty() ? current.data : null;
    }

    // set data current node
    public T setData(T x) {
        if (!isEmpty()) {
            current.data = x;
            return x;
        }

        return null;
    }

    //seek to first node
    public boolean first() {
        return seek(0);
    }

    // seek to next node
    public boolean next() {
        return seek(getIndex() + 1);
    }

    //seek to previous node
    public boolean previous() {
        return seek(getIndex() - 1);

    }

    //seek to last node
    public boolean last() {
        return seek(getSize() - 1);

    }

    //seek method
    public boolean seek(int loc) {
        if (isEmpty() || loc < 0 || loc >= getSize()) return false;

        // at least one element in list
        if (loc == 0) {
            current = front;
            index = 0;
        } else if (loc == getSize() - 1) {
            current = back;
            index = getSize() - 1;
        } else if (loc < index) {
            while (loc < index) {
                current = current.previous;
                index--;
            }
        } else if (loc > index) {
            while (loc > index) {
                current = current.next;
                index++;
            }
        }
        return true;
    }

    //insert first method
    public boolean insertFirst(T item) {
        seek(0);
        insertAt(item);

        return true;
    }

    //insert at current location (loc) method
    public boolean insertAt(T item) {
        DLListNode<T> newNode = new DLListNode<>(item);
        // if can insert empty => empty list => insert only one, otherwise its size >= 1
        if (insertEmpty(newNode)) return true;

        if (current.previous == null) {
            // since current.prev is null, meaning that we are at the start of the list
            newNode.next = current;
            current.previous = newNode;
            index = 0;
            front = newNode;
        } else {
            // else means current.prev is not null -> index does not change
            DLListNode<T> prev = current.previous;
            newNode.previous = prev;
            newNode.next = current;
            prev.next = newNode;
            current.previous = newNode;
        }
        current = newNode;
        size++;

        return true;
    }

    public boolean addBelowCurrent(T item) {
        if(next()) return insertAt(item);

        if(insertEmpty(new DLListNode<>(item))) return true;

        return insertLast(item);
    }

    //insert last method
    public boolean insertLast(T item) {
        DLListNode<T> newNode = new DLListNode<>(item);
        if (insertEmpty(newNode)) return true;

        // not empty list
        seek(getSize() - 1);

        current.next = newNode;
        newNode.previous = current;
        current = newNode;
        back = newNode;
        index++;
        size++;

        return true;

    }

    private boolean insertEmpty(DLListNode newNode) {
        if (!isEmpty()) return false;

        front = newNode;
        back = newNode;
        current = newNode;
        index = 0;
        size = 1;

        return true;
    }

    //delete first method
    public boolean deleteFirst() {
        // if seek first and delete true means have the first node
        return first() && deleteAt();
    }

    //delete at current location method
    public boolean deleteAt() {
        // 5 cases: empty, both prev and next are null, prev is null, next is null, have both prev and next
        if(isEmpty()) return false;

        DLListNode<T> next = current.next;
        DLListNode<T> prev = current.previous;

        // only one node
        if(getSize() == 1) {
            clear();
            return true;
        }
        // if get here means either or both next, prev not null, at least 2 nodes in list
        // prev null -> at start of list
        if(prev == null) {
            current.next = null;
            next.previous = null;
            current = next;
            front = next;
        }
        // next null -> end of list
        else if(current.next == null) {
            current.previous = null;
            prev.next = null;
            current = prev;
            back = prev;
            index--;
        }
        // else means both prev and next are not null so in middle of list
        else {
            current.previous = null;
            current.next = null;
            prev.next = next;
            next.previous = prev;
            current = next;
        }
        size--;

        return true;
    }

    //delete last method
    public boolean deleteLast() {
        return last() && deleteAt();
    }

    @Override
    public String toString() {
        return getDataInRange(0, getSize()-1);
    }

    public String getDataInRange(int start, int end) {
        if(isEmpty() ||start > end || start < 0 || end >= getSize()) return null;

        DLListNode<T> position = current;
        int i = getIndex();
        seek(start);

        StringBuilder data = new StringBuilder();
        while (start <= end) {
            data.append(current.data.toString());
            if(start != end) data.append("\n");
            next();
            start++;
        }
        current = position;
        index = i;

        return data.toString();
    }
}
 