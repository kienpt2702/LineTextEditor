JFLAGS = -g
JC = javac

.SUFFIXES: .java .class

.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
		  LTE.java \
		  CommandLine.java \
		  Buffer.java \
		  DLList.java

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class
