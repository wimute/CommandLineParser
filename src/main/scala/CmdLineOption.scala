package org.commandlineparser

/** Trait for a single option or parameter
*
* @author Christian Buschmann
*/
trait CmdLineOption{

  /** Trait which represents a single option or parameter
  * @author Christian Buschmann
  */
  trait CmdLineOptionItem{
    /** A list of option identifiers and aliases
    * 
    * e.g. ''-f'' or ''--filename''
    */
    val options: List[String] //Optionname and aliases e.g. List("-f","--filename")
    /** How many arguments are expected for this option 
    *
    * e.g. ''--filename'' would expect one argument, wich is the filename 
    */
    val expectedArgs: Int //How many Arguments are expected by this option e.g. 1 which is the filename
  }

  /** List of all known expected options */
  protected var expectedOptionsList: List[CmdLineOptionItem] = List.empty
  /** Add an option to the list of expected options
  * @param option to add
  */
  protected def addExpectedOption( option: CmdLineOptionItem ): Unit = 
    expectedOptionsList = expectedOptionsList :+ option

  /** Takes an array of arguments and groups all options with their 
  * corresponding parameters into a map. The information about options
  * and their paramters are taken from the expectedOptionsList. All arguments
  * which could not be found in expectedOptionsList are stored with an 
  * empty String key.
  *
  * e.g. ''--filename foo.txt -v anyParam'' could result in `Map( ( "--filename", List( "foo.txt" ) ), ( "-v", List() ), ( "", List( "anyParam" ) ) )`
  * if ''--fielname'' and ''-v'' are expected options and anyParam is a parameter
  * @param args Array of String which colud be provided by the main-function
  * @param index Index for args-array on which to start parsing 
  */
  protected def groupArgs( args: Array[String], index: Int = 0 ): Map[String,List[String]] =
    if( index < args.length ){
      val arg = args( index )
      expectedOptionsList.find( x => x.options.contains( arg ) ) match{
        case Some( option ) => { //put together with option
          val toIndex = index + option.expectedArgs + 1 
          val groupedArgs = groupArgs( args, toIndex )
          val newEntry =  if( groupedArgs contains arg )
                            ( arg, args.slice( index + 1, toIndex ).toList ++ groupedArgs( arg ) )
                          else
                            ( arg, args.slice( index + 1, toIndex ).toList )
          groupedArgs + newEntry
        }
        case None => { //put to parameters
          val groupedArgs = groupArgs( args, index + 1 )
          val newEntry = if( groupedArgs contains "" )
                           ( "", groupedArgs( "" ) :+ arg )
                         else //First entry in parameters
                           ( "", List( arg  ) )
          groupedArgs + newEntry 
        }
      }
    }else{
      Map.empty
    }
}

