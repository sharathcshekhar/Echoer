# Makefile is used to build the Echoer application
# See README for more details
# Dept. of Computer Science, University at Buffalo
# Project - 1, CSE-589
# Authors: Sharath Chandrashekhara, Sanketh Kulkarni
# 2012

FLAGS = 
JAVAC = javac

CLASSES = Echoer.java ConnectionStatus.java ValidateIP.java ConnectionListStore.java

default: all
	
all:
	@echo "Compiling files..." 	
	@$(JAVAC) $(FLAGS) $(CLASSES)
	@echo "Done" 	

clean:
	@rm -rf *.class
	@echo "Cleaned up the work space" 	
