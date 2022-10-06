public class Buffer {
    // Lots of stuff goes here
    private String filespec;
    private boolean dirty;
    private DLList<String> list;
    public static int count;

    public Buffer() {
        list = new DLList<>();
        filespec = "untitled" + count++;
        dirty = false;
    }

    public DLList<String> getList() {
        return list;
    }

    public String getFileName() {
        return filespec;
    }

    public boolean hasChanged() {
        return dirty;
    }

    public void setFileName(String filespec) {
        this.filespec = filespec;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public void setList(DLList<String> list) { this.list = list; }

    public void clear() {
        list.clear();
    }
}
