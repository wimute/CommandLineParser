package org.commandlineparser.example

import org.commandlineparser.CmdLineParser

import java.io.Reader
import java.io.Writer
import java.io.File
import java.io.FileReader
import java.io.BufferedReader
import java.io.LineNumberReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

object CatDemo {

  lazy val clp = new CmdLineParser{
    
    def parameter2InputStream( params: List[String] ): List[Reader] =
      if( params.size == 0 || params( 0 ) == '-' )
        List( new InputStreamReader( System.in ) )
      else
        params.map( fileString => new FileReader( new File( fileString ) ) )

    lazy val catParser = 
      for{
        params  <- params
      }yield cat( params, List.empty, new OutputStreamWriter( System.out ) )

    val hOption = so( List( "-h", "--help" ),   "Diese Hilfe ausgeben", 
                             help( "CatDemo", "DATEI(en) oder Standardeingabe auf Standardausgabe verketten." ) )
    val params = parameters( "[Datei]", "Ohne DATEI oder wenn DATEI „-“ ist, Standardeingabe lesen.", 
                             parameter2InputStream _ )
  }
 
  type ReaderFilter = Reader => Reader

  def cat( in: List[Reader], filter: List[ReaderFilter], out: Writer ): Unit =
    in.map( 
        reader =>filter.foldLeft( reader )( (readerPrev,filter) => filter( readerPrev ) ) )
      .foreach( 
        reader => {
          val bufferedReader = new BufferedReader( reader )
          var value = bufferedReader.read
          while( value > -1 ){
            out.write( value )
            value = bufferedReader.read
          } 
          out.flush 
        } )
  
  def main( args: Array[String] ): Unit = {

    clp.hOption( args ) match{
      case Some( help ) => println( help )
      case None => clp.catParser( args )
    }
  }
}

