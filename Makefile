all:
	javac behavior/*.java placement/*.java ./*.java

clean:
	rm *.class behavior/*.class placement/*.class