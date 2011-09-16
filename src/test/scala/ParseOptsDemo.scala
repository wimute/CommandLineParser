package org.commandlineparser

//Demo

object ParseOptsDemo {

  def main( args: Array[String] ): Unit = {
    type Plugin = () => Int 

    def calculate( foo: Int, bar: Plugin, blubb: Int ): Int = foo + bar() + blubb
    def doSmth2( foo: Int ): Plugin = () => foo 
    def gui( foo: Int, blubb: Int ): Int = foo + blubb

    val clp = new CmdLineParser{
                lazy val doSmth2M = for{
                                      c <- cOption
                                    }yield doSmth2( c )

                lazy val calculateM = for{
                                        b <- bOption
                                        p <- pOption
                                        o <- otherVals
                                      }yield calculate( b, p, o )

                lazy val guiM = for{
                                  b <- bOption
                                  o <- otherVals
                                }yield gui( b, o )

                lazy val helpOption = so( List( "-h", "--help" ), "Print this help", help( "test", "Testdescription" ) )
                lazy val guiOption =  so( List( "-g", "--gui" ),  "Start with gui",  guiM )
                lazy val vOption = so(   "-v", "Verbose", 1, 0 )
                lazy val cOption = sop(  "-c", "<int>", "BlaBlubb zu c", _.toInt, 0 )
                lazy val pOption = so(   "-p", "Foobar zu p", doSmth2M, () => 0 )
                lazy val bOption = sopl( "-b", "<int>,<int>,...", "Bar zu b", ",", _.foldLeft(0)((x,y) => x + y.toInt), 0 )
                lazy val otherVals = parameters( "<int> <int> ...", "Values to add", _.foldLeft(0)((x,y) => x + y.toInt ) )
    }

    clp.helpOption( args ) match{
      case Some( help ) => println( help )
      case None => println( clp.guiOption( args ) match{
                              case Some( guiResult ) => guiResult
                              case None => clp.calculateM( args )
                            } )
    }
  }
}

