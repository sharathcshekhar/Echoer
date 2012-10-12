FLAGS = 
JAVAC = javac

CLASSES = TestServer.java ConnectionStatus.java

default: all
	
all:
	$(JAVAC) $(FLAGS) $(CLASSES) 

clean:
	rm -rf *.class
