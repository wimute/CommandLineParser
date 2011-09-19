package org.commandlineparser.example

import org.commandlineparser.CmdLineParser


/** Running demo from README 
*/
object ReadmeDemo {

  def func1( i: Int ): Int = i

  val clp = new CmdLineParser{
    def program: CmdLineParser[Int] = for{
                                        a <- aOption 
                                      }yield func1( a )

    val aOption = sop( "-a", "<int>", "Integer value as parameter for this option", _.toInt, 0 )
    val params = parameters( "<aString> <aString> ...", "Strings to echo", _.foreach( string => println ( string ) ) )
    }

  def main( args: Array[String] ): Unit = {
    
    println( clp.aOption( args ) )

    println( clp.program( args ) )

    println( clp.help( "ReadmeDemo", "Description for program ReadmeDemo" ) )

  }
}

