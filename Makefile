FLAGS = 
JAVAC = javac

CLASSES = Echoer.java ConnectionStatus.java ValidateIP.java ConnectionListStore.java

default: all
	
all:
	@$(JAVAC) $(FLAGS) $(CLASSES) 

clean:
	@rm -rf *.class
