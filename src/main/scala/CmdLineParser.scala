package org.commandlineparser

/** This trait produces CmdLineOptions and defines a function which takes an
* Array[String] and produces the expected value
* @author Christian Buschmann
*/
trait CmdLineParser extends CmdLineOption with CmdLineHelp{
 
  /** This type defines a parser which takes an Array of string as command line options
  * and results in any expected Value */
  type Parser[+A] = ( Array[String] => A )

  /** The CmdLineParser in monadic style
  *
  * The parser is not consuming tokens, so that the array
  * remains complete during parsing
  * @author Christian Buschmann
  */
  trait CmdLineParser[+A] extends Parser[A]{
    def map[B]( f: A => B ): CmdLineParser[B] = 
      unit( in => f( apply( in ) ) )
    def flatMap[B]( f: A => CmdLineParser[B] ): CmdLineParser[B] =
      unit( in => f( apply( in ) )( in ) )
  }

  /** Produce a new command line parser
  * @param parserFunc A function which extracts a value form an array of strings
  * @return Returns the new command line parser
  */
  def unit[A]( parserFunc: Parser[A] ): CmdLineParser[A] = 
    new CmdLineParser[A]{
      def apply( in: Array[String] ) = parserFunc( in )
    }

  /** Registers a new option as known option and known help option
  * @param option Option to add
  * @return Returns the command line parser
  */
  protected def addOptionItemHelp[A]( option: CmdLineParser[A] with CmdLineOptionItem with CmdLineOptionHelp ): CmdLineParser[A] = {
    addExpectedOption( option )
    addHelpOption( option )
    option
  }

  /** Registers a new option as known option and known help option
  * @param option Option to add
  * @return Returns the command line parser
  */
  protected def addOptionHelp[A]( option: CmdLineParser[A] with CmdLineOptionHelp ): CmdLineParser[A] = {
    addHelpOption( option )
    option
  }

  /** Creates a new option with the given paramters and registers the option as known
  * option and help option
  * @param parserFunc Function to extract a value from an array of strings
  * @param optionsIn The option identifier and aliases as list of strings
  * @param expectedArgsIn How many arguments does this option expects
  * @param helpOptionIn String reprensentation of this option identifiers and aliases for help output
  * @param helpDescriptionIn Description for this option
  * @return a command line parser
  */
  def createOption[A]( parserFunc: Parser[A], 
                       optionsIn: List[String], 
                       expectedArgsIn: Int, 
                       helpParameterIn: String, 
                       helpDescriptionIn: String ): CmdLineParser[A] =
    addOptionItemHelp( 
      new CmdLineParser[A] with CmdLineOptionItem with CmdLineOptionHelp{
        def apply( in: Array[String] ) = parserFunc( in )

        val options: List[String] = optionsIn
        val expectedArgs: Int = expectedArgsIn
        val helpOption: List[String] = optionsIn
        val helpParameter: String = helpParameterIn
        val helpDescription: String = helpDescriptionIn
      } )

  /** Creates a non-option with help
  * @param parserFunc Function to extract a value from an array of strings
  * @param helpOptionIn String reprensentation of this option identifiers and aliases for help output
  * @param helpDescriptionIn Description for this option
  * @return a command line parser
  */
  def createParameters[A]( parserFunc: Parser[A], 
                           helpParameterIn: String, 
                           helpDescriptionIn: String ): CmdLineParser[A] =
    addOptionHelp( 
      new CmdLineParser[A] with CmdLineOptionHelp{
        def apply( in: Array[String] ) = parserFunc( in )

        val helpOption: List[String] = List.empty
        val helpParameter: String = helpParameterIn
        val helpDescription: String = helpDescriptionIn
      } )

  /** Wraps a string into a string-list
  *
  * e.g. ''"Foo"'' becomes ''List( "Foo" )''
  * @param value String to wrap in List
  * @return List with string
  */
  implicit def string2List( value: => String ): List[String] = List( value )
  /** Wraps any value into a CmdLineParser
  * @param value Value to wrap
  * @return CmdLineList with value
  */
  implicit def any2CmdLineParser[A]( value: => A ): CmdLineParser[A] = unit( args => value )
  /** Wraps any Function1 into a CmdLineParser
  * @param value Value to wrap
  * @return CmdLineList with value
  */
  implicit def func12CmdLineParser[A]( func1: String => A ): String => CmdLineParser[A] = value => unit( args => func1( value ) )
  /** Wraps any Function1 with List[String] as parameter into a CmdLineParser
  * @param value Value to wrap
  * @return CmdLineList with value
  */
  implicit def func1List2CmdLineParser[A]( func1: List[String] => A ): List[String] => CmdLineParser[A] = params => unit( args => func1( params ) )

  /** Add a simple option without any paramters
  * @param options Option identifier and aliases 
  * @param helpDescription Description for this option
  * @param successVal Value if option is found
  * @param defaultVal Value if option is not found
  * @return a command line Parser
  */
  def so[A]( options: List[String], helpDescription: String, successVal: CmdLineParser[A], defaultVal: CmdLineParser[A] ): CmdLineParser[A] = 
    createOption( in => if( groupArgs( in ) exists { entry => options.contains( entry._1 ) } )
                          successVal( in )
                        else
                          defaultVal( in ), options, 0, "", helpDescription ) 

  /** Add a simple option without any paramters. Returns an option
  * @param options Option identifier and aliases 
  * @param helpDescription Description for this option
  * @param successVal Value if option is found
  * @return a command line Parser which returns an Option[A]
  */
  def so[A]( options: List[String], helpDescription: String, successVal: CmdLineParser[A] ): CmdLineParser[Option[A]] = 
    createOption( in => if( groupArgs( in ) exists { entry => options.contains( entry._1 ) } )
                          Some( successVal( in ) )
                        else
                          None, options, 0, "", helpDescription ) 

  /** Add an option with one paramter
  * @param options Option identifier and aliases 
  * @param helpParameter Parameter for this option e.g. ''<filename>''
  * @param helpDescription Description for this option
  * @param successVal Function with one parameter if option is found
  * @param defaultVal Value if option is not found
  * @return a command line Parser
  */
  def sop[A]( options: List[String], helpParameter: String, helpDescription: String, successVal: String => CmdLineParser[A], defaultVal: CmdLineParser[A] ): CmdLineParser[A] = 
    createOption( in => groupArgs( in ) find { entry => options.contains( entry._1 ) } match {
                          case Some( entry ) => successVal( entry._2 head )( in )
                          case None => defaultVal( in )
                        }, options, 1, helpParameter, helpDescription )

  /** Add an option with one paramter. Returns an option
  * @param options Option identifier and aliases 
  * @param helpParameter Parameter for this option e.g. ''<filename>''
  * @param helpDescription Description for this option
  * @param successVal Function with one parameter if option is found
  * @return a command line Parser which returns an Option[A]
  */
  def sop[A]( options: List[String], helpParameter: String, helpDescription: String, successVal: String => CmdLineParser[A] ): CmdLineParser[Option[A]] = 
    createOption( in => groupArgs( in ) find { entry => options.contains( entry._1 ) } match {
                          case Some( entry ) => Some( successVal( entry._2 head )( in ) )
                          case None => None
                        }, options, 1, helpParameter, helpDescription )

  /** Add an option with paramterlist
  * @param options Option identifier and aliases 
  * @param helpParameter Parameter for this option e.g. ''<filename1>,<filename2>,...''
  * @param helpDescription Description for this option
  * @param delim Delimiter for list e.g. '',''
  * @param successVal Function with one parameter if option is found
  * @param defaultVal Value if option is not found
  * @return a command line Parser
  */
  def sopl[A]( options: List[String], helpParameter: String, helpDescription: String, delim: String, 
               successVal: List[String] => CmdLineParser[A], defaultVal: CmdLineParser[A] ): CmdLineParser[A] = 
    createOption( in => groupArgs( in ) find { entry => options.contains( entry._1 ) } match {
                          case Some( entry ) => successVal( entry._2.head.split( delim ).toList )( in )
                          case None => defaultVal( in )
                        }, options, 1, helpParameter, helpDescription )

  /** Add an option with paramterlist. Returns an option
  * @param options Option identifier and aliases 
  * @param helpParameter Parameter for this option e.g. ''<filename1>,<filename2>,...''
  * @param helpDescription Description for this option
  * @param delim Delimiter for list e.g. '',''
  * @param successVal Function with one parameter if option is found
  * @return a command line Parser which returns an Option[A]
  */
  def sopl[A]( options: List[String], helpParameter: String, helpDescription: String, delim: String, 
               successVal: List[String] => CmdLineParser[A] ): CmdLineParser[Option[A]] = 
    createOption( in => groupArgs( in ) find { entry => options.contains( entry._1 ) } match {
                          case Some( entry ) => Some( successVal( entry._2.head.split( delim ).toList )( in ) )
                          case None => None
                        }, options, 1, helpParameter, helpDescription )

  /** All other parameters
  * @param parametersVal Function to be executed on all other parameters 
  * @param helpParameter Parameter help e.g. ''<filename1> <filename2>''
  * @param helpDescription Description for parameters e.e. ''Specify filenames''
  * @return a command line Parser
  */
  def parameters[A]( helpParameter: String, helpDescription: String, parametersVal: List[String] => CmdLineParser[A] ): CmdLineParser[A] =
    createParameters( in => { val groupedArgs = groupArgs( in )
                              val parametersVals = if( groupedArgs contains "" )
                                                     groupedArgs( "" )
                                                   else
                                                     List.empty 
                              parametersVal( parametersVals )( in ) }, helpParameter, helpDescription )
}

