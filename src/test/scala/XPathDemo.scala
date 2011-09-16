package org.commandlineparser.example

import org.commandlineparser.CmdLineParser

import javax.xml.xpath.XPathFactory
import javax.xml.xpath.XPathExpression

import org.xml.sax.InputSource

import javax.xml.namespace.NamespaceContext

import java.io.Reader
import java.io.Writer
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter

object XPathDemo {

  lazy val clp = new CmdLineParser{
    
    lazy val evaluateXPathParser = 
      for{
        in <- fOption
        xpath  <- xpath
        out <- oOption
      }yield evaluateXPath( in, xpath, out )

    lazy val defaultInReader: Reader = new InputStreamReader( System.in )
    lazy val defaultOutReader: Writer = new OutputStreamWriter( System.out )

    def inFile2Reader( file: String ): Reader =
      if( file == '-' )
        defaultInReader
      else
        new FileReader( new File( file ) )

    def string2NamespaceContext( ns: List[String] ): NamespaceContext =
      new NamespaceContext{
        def splitNamespace( ns: String ): ( String, String ) = {
          val nsSplit = ns.splitAt( ns.indexOf( ':' ) )
          ( nsSplit._1, nsSplit._2.drop( 1 ) )
        }

        lazy val prefix2Namespace = ns.foldLeft( Map.empty[String,String] )( (map,str) => map + splitNamespace( str ) )
        lazy val namespace2Prefix = ns.foldLeft( Map.empty[String,String] )( (map,str) => map + splitNamespace( str ).swap )
        
        def getNamespaceURI( prefix: String ): String = prefix2Namespace( prefix )
        def getPrefix( namespaceURI: String ): String = namespace2Prefix( namespaceURI )
        def getPrefixes( namespaceURI: String ): java.util.Iterator[String] = new java.util.Iterator[String]{ 
                                                                                var value: Option[String] = Some( namespace2Prefix( namespaceURI ) )  
                                                                                def hasNext(): Boolean = value.isDefined
                                                                                def next(): String = value match{
                                                                                  case Some(valueStr) => { value = None;  valueStr }
                                                                                  case None        => null 
                                                                                }
                                                                                def remove( ): Unit = {}
                                                                              }
      }

    def string2XPath( xpaths: List[String] ): CmdLineParser[XPathExpression] = 
      for{
        ns <- nOption
      }yield{
        val xpath = XPathFactory.newInstance( ).newXPath
        xpath.setNamespaceContext( ns )
        xpath.compile( xpaths head )
      }


    val hOption =   so( List( "-h", "--help" ),      "Diese Hilfe ausgeben", 
                             help( "XPathDemo", "Ein XPath-Ausdruck wird auf eine XML-Datei angewendet." ) )
    val fOption =  sop( List( "-f", "--file" ),      "<XML-Datei>", "Ohne diese Option oder wenn XML-DATEI „-“ ist, Standardeingabe lesen.",
                             inFile2Reader, defaultInReader )
    val oOption =  sop( List( "-o", "--output" ),    "<Datei>", "Ausgabedatei in welche das Ergebnis geschrieben wird. Ohne diese Option wird in die Standardausgabe geschrieben.", 
                             file => new FileWriter( new File( file ) ), defaultOutReader )
    val nOption = sopl( List( "-n", "--namespace" ), "<prefix:ns>,<prefix:ns>,...", "Angabe einer Reihe von Namespaces auf welche im XPath benötigt werden.", ",",
                             string2NamespaceContext _, string2NamespaceContext( List.empty ) )
    val xpath = parameters( "<XPath>", "Der anzuwendene XPath-Ausdruck.", 
                             string2XPath )
  }
 
  type ReaderFilter = Reader => Reader

  def evaluateXPath( in: Reader, xpath: XPathExpression, out: Writer ): Unit = {
    out.write( xpath.evaluate( new InputSource( in ) ) )
    out.flush
  }

  def main( args: Array[String] ): Unit = {

    clp.hOption( args ) match{
      case Some( help ) => println( help )
      case None => clp.evaluateXPathParser( args )
    }
  }
}

