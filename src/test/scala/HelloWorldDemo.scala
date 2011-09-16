package org.commandlineparser.example

import org.commandlineparser.CmdLineParser


/** This simple "HelloWorld" program adds some numbers.
* Optionally you can set an initial value for your addition
* e.g. ''HelloWorldDemo --startVal 10 1 2 3'' prints ''16''
*/
object HelloWorldDemo {

  val clp = new CmdLineParser{

    /** This functions receives all parameters of this program, treats them as numbers and sums them up.
    * If ''--startVal'' option is specified than add everything else to this value */
    def calcParser( params: List[String] ): CmdLineParser[Int] = 
      for{
        startVal <- startValOption //Read the parameter of ''--startVal'' as Int
      }yield params.foldLeft( startVal )( ( x, y ) => x + y.toInt ) // Here the calculation takes place. Everything is summed up
    
    //The following three line defines all commandline options for this program and 
    //their corresponding functions 
    //Ín general you define an option together with a function which will be evaluated,
    //if the option is given on command line. A default function provides a value, if
    //the option is not given.
    /** Define the help option */
    val helpOption = so( List( "-h", "--help" ), "Print this help", help( "HelloWorldDemo", "You can add and subtract..." ) )

    /** Define the start value. If this option is set, than return the number specified 
    * on command line (_.toInt), otherwise return ''0'' as default
    * ''sop'' is short for (s)ingle (o)ption with (p)arameter
    * There is also ''so'' (s)ingle (o)ption and ''sopl'' (s)ingle (o)ption with (p)arameter (l)ist*/
    val startValOption = sop( "--startVal", "<int>", "Start value for addition", _.toInt, 0 )

    /** Defines the function which takes all parameters. In this case all parameters will 
    * be treated as numbers and added */
    val programParser = parameters( "<int> <int> ...", "Values to add", calcParser ) 
    }

  def main( args: Array[String] ): Unit = {

    clp.helpOption( args ) match{ //Check if help is requested
      case Some( help ) => println( help )
      case None => println( clp.programParser( args ) ) // run the calculation
    }
  }
}

