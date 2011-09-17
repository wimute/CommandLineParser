package org.commandlineparser.example

import org.commandlineparser.CmdLineParser


/** This simple "HelloWorld" program adds some numbers.
* Optionally you can set an initial value for your addition
* 
* e.g. ''HelloWorldDemo 1 2 3 --startVal 10'' prints ''16''
*  and ''HelloWorldDemo 1 2 3'' prints ''6''
*/
object HelloWorldDemo {

  val clp = new CmdLineParser{

    /** This functions receives all parameters of this program, treats them as numbers and sums them up.
    * If ''--startVal'' option is specified than add everything else to this value.
    * You can see how options can be combined. calcParser consumes the result of startValOption*/
    def calcParser( params: List[String] ): CmdLineParser[Int] = 
      for{
        startVal <- startValOption //Get an int value from ''--startVal''-option
      }yield params.foldLeft( startVal )( ( x, y ) => x + y.toInt ) // Here the calculation takes place. Everything is summed up
    
    /**The following three ''val'' define all commandline options for this program and 
    * their corresponding functions .
    *
    * Ín general every option is a parser on its own (type ''CmdLineParser[A]'' is a function ''Array[String] => A''). 
    * So every option can be evaluated by option( args ). Options can also be combined, if some option
    * depends on a another one (see def calcParser for an example).
    *
    * Define the help option 
    * helpOption has type CmdLineParser[Option[String]] */
    val helpOption = so( List( "-h", "--help" ), "Print this help", help( "HelloWorldDemo", "You can add and subtract..." ) )

    /** Define the start value. If this option is set, than return the number given as 
    * corresponding parameter (_.toInt), otherwise return ''0'' as default.
    *
    * ''sop'' is a builder function for options. You can set the option identifier, some 
    * explanations for help and finally the value or function which will be returned / evaluated.
    * You define a value / function as ''succesVal'' if the option is found on command line and a
    * value / function as ''defaultVal'', if the option is not present on command line.
    * If you skip the defaultVal the Result would be an Option[A] (see val helpOption).
    *
    * Values and functions are provided as type ''CmdLineParser[A]'' to every builder function.
    * There are implicits defined which auto-wraps any value or function to CmdLineParser[A].
    * The implicits take their parmeter ''by reference'', so that functions are evaluated lazy.
    *
    * The option identifiers and aliases are provided as List[String]. There is also an implicit
    * defined which converts a single String to a List[String].
    *
    * ''sop'' is short for (s)ingle (o)ption with (p)arameter
    * There is also ''so'' (s)ingle (o)ption and ''sopl'' (s)ingle (o)ption with (p)arameter (l)ist
    * See trait CndLineParser for details
    *
    * startValOption has type CmdLineParser[Int] */
    val startValOption = sop( "--startVal", "<int>", "Start value for addition", _.toInt, 0 ) 

    /** Defines the function which takes all parameters (all non options). In this case all parameters will 
    * be treated as numbers and added. 
    * 
    * programParser has type CmdLineParser[Int] */
    val programParser = parameters( "<int> <int> ...", "Values to add", calcParser ) 
    }

  def main( args: Array[String] ): Unit = {

    clp.helpOption( args ) match{ //Check if help is requested
      case Some( help ) => println( help )
      case None => println( clp.programParser( args ) ) // run the calculation
    }
  }
}

