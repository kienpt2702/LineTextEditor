public class Buffer {
    // Lots of stuff goes here
    private String filespec;
    private boolean dirty;
    private DLList<String> list;


    public Buffer() {

    }

    public DLList<String> getList() {
        return list;
    }

    public String getFilespec() {
        return filespec;
    }

    public boolean getDirty() {
        return dirty;
    }

    public void setFilespec(String filespec) {
        this.filespec = filespec;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }
}
