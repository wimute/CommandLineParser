# Command line parser for scala

Simple scala library which helps to attach values / functions to comand line options. 

## How to use options in your program

Basicly you can define a command line option as a scala `val`. Every option is 
a parser on its own, which takes an `Array[String]` and results to the 
expected value ( `type CmdLineParser[A]` is a function `Array[String] => A` ).
  
    def main( args: Array[String] ): Unit = {
      ...
      val aOption: CmdLineParser[Int] = ... //definition of an "-a" option which returns an Integer
      aOption( args ) //Returns the Int-value provided by this option

How to get an instance for aOption is described below.

Parsers can be combined in a monadic style.
E.g. if function func1 needs the value from option a, you can do this:

      def program: CmdLineParser[Int] = for{
                                          a <- aOption 
                                        }yield func1( a )
      program( args )

You can also add a description to every option, so that a complete formatted help 
for your program is generated.

## How to create options

The trait CmdLineParser helps you to build the options. There are are three 
types of options you can define with functions from CmdLineParser:

  * so: An option without any parameter (e.g. --verbose)
  * sop: An option which takes a parameter (e.g. --file <filename>)
  * sopl: An option which takes a list of parameters (e.g. --files <filename>,<filename>,...)

E.g. above aOption could be defined this way:

    val clp = 
      new CmdLineParser{
        val aOption = sop( "-a", "<int>", "Integer value as parameter for this option", _.toInt, 0 )
      }

  The parameter `_.toInt` is the successVal, which will be evaluated, if the option is found
  on command line.
  The parameter `0` is the defaultVal, which will be returned, if the option is not found
  on command line.
  If you skip the defaultVal, then the return value will be wrapped into an `Option[A]`.

For all non-option parameters on the command line you can use the function `parameters` from
CmdLineParser.

    val params = parameters( "<aString> <aString> ...", "Strings to echo", _.foreach( string => println ( string ) ) )

## How to generate program help

If you want to get the generated program help just call the `help` function from CmdLineParser

    println( clp.help( "ReadmeDemo", "Description for program ReadmeDemo" ) )

should output

> Usage: ReadmeDemo [OPTIONS] \<aString\> \<aString\> ...
> 
> Description for program ReadmeDemo
> 
> OPTIONS
> 
> -a \<int\>  Integer value as parameter for this option
> 
> PARAMETERS
> 
> \<aString\> \<aString\> ...  Strings to echo 

## Further reading and example

For a more detailed description see src/test/scala/HelloWorldDemo.scala as an example
and see the methods in src/main/scala/CmdLineParser.scala

You can run the HelloWorld example with sbt.
Enter `test:run 1 1 2 3 5 --startVal 25` on sbt-prompt and choose HelloWorldDemo for execution.

## Copyright
(c) 2011 Christian Buschmann, released under the BSD license

