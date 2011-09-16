package org.commandlineparser

/** Trait to display help for a program and its options.
* Every defined option and parameter will be stored in ''helpOptionsList''
* as known help option. 
* A call to method ''help'' will produce a complete formatted program help.
*
* An option always starts with an identifier (e.g. ''--file''), while 
* a parameter has no identifier.
* 
* @author Christian Buschmann
*/
trait CmdLineHelp{

  /** This trait enables help capability to a single option or parameter
  @author Christian Buschmann
  */
  trait CmdLineOptionHelp{
    /** help-identifier for the option and aliases e.g. ''--file'' */
    val helpOption: List[String] 
    /** Short description for the parameter e.g. ''<filename>'' */
    val helpParameter: String 
    /** Long description for this option/parameter e.g. ''Set the filename to read from'' */
    val helpDescription: String 

    /** Produces a description for this specific option / paramter withits short description.
    * Every option identifier will be concatenated with its parameter description.
    * E.g. List( "-f <filename>", "--filename <filename>" )
    * @param delim Delimiter between option and its parameter. Default is space
    * @return Option/paramter with its short description
    */
    def helpOptionParameter( delim: String = " " ): List[String] = 
      if( helpOption.length > 0 )
        helpOption.map( opt => opt + delim + helpParameter )
      else
        List( helpParameter )
    
    /** Maximum length of this option/paramter with its short description
    * @param delim Delimiter between option and its parameter. Default is space
    * @return Maximum length of this option/paramter without description
    */
    def helpOptionParameterMaxLength( delim: String = " " ): Int = 
      helpOptionParameter( delim ).foldLeft(0)( (x,y) => if( y.length > x  ) y.length else x ) 

    /** Formats the long description to a specific line length.
    * Every list-item becomes a row which length is less or 
    * equal to  maxLength
    * 
    * @param maxLength Maximum length for a single line
    * @return description in list with each item <= maxLength
    */
    def formatDescription( maxLength: Int ): List[String] = 
      formatStringToLength( helpDescription, maxLength )

    private def formatDescriptionLine( maxLength: Int, line: String ): List[String] = {
      val lineTrim = line.trim
      if( lineTrim.length <= maxLength ){
        List( lineTrim )
      }else{
        val indexToSplit = lineTrim.lastIndexOf( ' ', maxLength )
        val lineSplittet = lineTrim.splitAt( indexToSplit )
        lineSplittet._1.trim :: formatDescriptionLine( maxLength, lineSplittet._2 )
      }
    }
        
  }

  /** All known help-infos for options  and parameters*/
  protected var helpOptionsList: List[CmdLineOptionHelp] = List.empty
  /** Adds a new help option or parameter to the list of known-help-options
  * @param option help option to add to knwon-help-options
  */
  protected def addHelpOption( option: CmdLineOptionHelp ): Unit = 
    helpOptionsList = helpOptionsList :+ option
  /** Get only options
  * @return All options
  */
  protected def getOptions: List[CmdLineOptionHelp] = helpOptionsList.filter( x => x.helpOption.length > 0 )
  /** Get only parameters
  * @return All parameters
  */
  protected def getParameters: List[CmdLineOptionHelp] = helpOptionsList.filter( x => x.helpOption.length == 0 )


  /** Formats a any text to a specific length
  * @param text Text to format
  * @param maxLength Maximum length for a single line
  * @return description in table with each item <= maxLength
  */
  def formatStringToLength( text:String, maxLength: Int = 74 ): List[String] = {
    val lines = text.lines.toList
    lines.flatMap( line => formatStringToLengthLine( maxLength, line ) )
  }

  private def formatStringToLengthLine( maxLength: Int, line: String ): List[String] = {
    val lineTrim = line.trim
    if( lineTrim.length <= maxLength ){
      List( lineTrim )
    }else{
      val indexToSplit = lineTrim.lastIndexOf( ' ', maxLength )
      val lineSplittet = lineTrim.splitAt( indexToSplit )
      lineSplittet._1.trim :: formatStringToLengthLine( maxLength, lineSplittet._2 )
    }
  }

  /** Print a single option/parameter with long description formatted for output
  * @param option Option to print
  * @param indexDescription Index to start description
  * @param descriptionLength Length of a line in description text
  * @return Detailed option
  */
  protected def detailOptionHelp( option: CmdLineOptionHelp, indexDescription: Int, descriptionLength: Int ): String = {
    val optionParameter = option.helpOptionParameter( )
    val optionParameterString = optionParameter.foldLeft( "" )( ( x,y ) => x + y + "\n" ).dropRight( 1 )
    val fillUpLast = " " * ( indexDescription - optionParameter.last.length )
    val fillUpToDescription = " " * indexDescription
    val formatDescription = option.formatDescription( descriptionLength )
    val formatDescriptionHeadString = formatDescription.head
    val formatDescriptionTailString = 
      if( formatDescription.length > 1 )
        "\n" + formatDescription.tail.foldLeft( "" )( ( x,y ) => x + fillUpToDescription + y + "\n" ).dropRight( 1 )
      else 
        ""

    optionParameterString + fillUpLast + formatDescriptionHeadString + formatDescriptionTailString
  }
    

  /** Produces a help for all known options
  * formatted or output
  *
  * e.g. 
  * ''-o   file for output''
  * ''-v   be verbose''
  *
  * @param lfength Maximal length of the help
  * @return Formatted option help
  */
  protected def helpOptionsDetail( length: Int = 74 ): String = {
    val options = getOptions
    if( options.length > 0 )
      "OPTIONS\n\n" + helpDetail( getOptions, length )
    else
      ""
  }

  /** Produces an output formatted help for 
  * parameters
  *
  * e.g. 
  * ''-o   file for output''
  * ''-v   be verbose''
  *
  * @param length Maximal length of the help
  * @return Formatted parameter help
  */
  protected def helpParametersDetail( length: Int = 74 ): String = { 
    val parameters = getParameters
    if( parameters.length > 0 )
      "PARAMETERS\n\n" + helpDetail( parameters, length )
    else
      ""
  }

  /** Produces an output formatted help for the given 
  * CmdLineOptions
  *
  * e.g. 
  * ''-o   file for output''
  * ''-v   be verbose''
  *
  * @param length Maximal length of the help
  * @return Multiline help
  */
  protected def helpDetail( helpOptionParameter: List[CmdLineOptionHelp], length: Int = 74 ): String = {
    val longestHelpOptionParameter = helpOptionParameter.foldLeft( 0 )( 
                                      (x,y) => 
                                        if( y.helpOptionParameterMaxLength( ) > x ) 
                                          y.helpOptionParameterMaxLength( ) 
                                        else 
                                          x 
                                     ) + 2 
    helpOptionParameter.foldLeft( "" )( (x,y) => x + detailOptionHelp( y, longestHelpOptionParameter, ( length - longestHelpOptionParameter ) ) + "\n" )
  }

  /** Prints a usage-hint for this program 
  * Usually the first line in every program help
  *
  * e.g. ''Usage: prog [OPTIONS]''
  * @return help-options in a row
  */
  protected def helpOptionsShort( progname: String = "" ): String = {
    val options = if( getOptions.length > 0 ) " [OPTIONS]" else " "
    "Usage: " + progname + options + getParameters.foldLeft("")( (x,y) => x + " " + y.helpParameter )
  }

  /** Produces the complete program help formatted for output
  * @return Program help text
  */
  def help( progname: String = "", progDescription: String = "" ): String = {
    val progDescriptionString = formatStringToLength( progDescription ).foldLeft( "" )( ( x,y ) => x + y + "\n" ).dropRight( 1 )
    val progDescriptionStringFilled = if( progDescriptionString.trim.length > 0 ) "\n" + progDescriptionString + "\n" else ""
    "\n" + helpOptionsShort( progname ) + "\n" + progDescriptionStringFilled + "\n" + helpOptionsDetail( ) + "\n" + helpParametersDetail( )
  }
}

