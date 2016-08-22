import xml.XML._
import scala.xml._
import java.io._
import scala.collection.mutable.ArrayBuffer
import java.util.HashMap


class Component(id_in: String, pp_in: Elem){
  val id = id_in; 
  val pp = pp_in;
  val requirements = new ArrayBuffer[Node];

  /**
    * 
    */
  def addRequirement(req: Node){
    if(req != null){
      requirements.append(req);
    }
  }

  /**
    * 
    */
  def reqCount() = requirements.size

  /**
    * 
    */
  def toHtml(out: PrintStream, amIFirst: Boolean){
    if(reqCount>0){
      if(!amIFirst){
        out.println("<tr>");
      }


      out.println("<td class='subsection' rowspan='"+reqCount+"'>"+id+"</td>")
      var notFirst="";
      for(aa <- requirements){
        out.println(notFirst);
        out.println(reqResultToHtml(aa));
        out.println("</tr>");
        notFirst="<tr>";
      }
      out.println("<!-- End subsection -->");
    }
  }

  /**
    * Covert a Node to html output
    */
  def reqResultToHtml(req: Node)={
    val ppReq = ApplicationProtectionProfile.findRequirement(pp, (req\"@id").text);

    val appliedText = 
      if( (req\"applied-text").size !=0)
        NodeHtmlizer.printNodeSeq(req\"applied-text")
      else 
        NodeHtmlizer.printNodeSeq(ppReq\"title")
    val results = 
      if (req\"passed" != null)
        "</td><td class='result'>passed</td><td class='summary'><div class='summary-div'>"+(req\"activity"\"summary").text +"</div></td>" 
      else 
        "";

    "<td class='req-id'>"+ req\"@id" +"</td><td class='apptext'><div class='title-div'>"+ appliedText +"</div></td><td class='req-note'><div class='note-div'>"+ NodeHtmlizer.printNodeSeq(ppReq\"note") +"</div>"+ results;
  }
}


class SubSection(id_in: String, pp_in: Elem){
  val id = id_in;
  val pp = pp_in;
  val components = new ArrayBuffer[Component]();



  /**
    * 
    */
  def addRequirement(req: Node){
    if(components.last == null){
      throw new RuntimeException("Encountered orphan requirement");
    }
    else{
      components.last.addRequirement(req);
    }
  }


  /**
    * 
    */
  def reqCount():Int ={
    var ret = 0;
    for( aa <- components){
      ret+=aa.reqCount
    }
    ret;
  }

  /**
    * 
    */
  def toHtml(out:PrintStream, amIFirst:Boolean){
    if(reqCount>0){                                                // If there are requirements
      if(!amIFirst ){                                              // If it's not the first...
        out.println("<tr>");                                       // ...start a new row
      }
      out.println("<td class='section' rowspan='"+reqCount+"'>"+id+"</td>")        // Insert cell
      var isFirst = true;                                          // Create a variable that's true only for the first run
      for(component <- components){                                // Go through all the components
        component.toHtml(out,isFirst);                             // Print out the component
        if(component.reqCount > 0){
          isFirst = false;                                         // Set the true-only-first to false
        }
      }
    }
  }
}

/**
  * 
  */
class ReportMaker(val report_in: Elem, val pp_in: Elem, val out_in: PrintWriter){
  // Holds the XML report
  val report = report_in;

  // Holds off the PP we care about
  val pp = pp_in;

  // Holds the handle to the HTML output
  val out = out_in;

  // Holds on the subsections
  val subsections = new ArrayBuffer[SubSection]();

  /**
    * 
    */
  def createReport(){
    for(sub <- pp \\ "subsection"){
      val subsection = new SubSection(sub\"@id"+"", pp);

      for(comp <- sub \ "f-component"){
        val component = handleFComponent(comp);
        if(component.reqCount > 0 ){
          subsection.components.append(component);
        }
      }
      for(comp <- sub \ "a-component"){
        val component = handleAComponent(comp);
        if(component.reqCount > 0 ){
          subsection.components.append(component);
        }
      }

      if(subsection.reqCount > 0){
        subsections.append(subsection);
      }

    }


    var isFirst = true;

    println(
"""<?xml version="1.0"?>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <title></title>
    <style type="text/css">
      table, th, tr{
        border: 1px black;
      }

      .clickable:hover { cursor:pointer;}
      .tg  {border-collapse:collapse;border-spacing:0;border-color:#aabcfe;}
      .tg td{
         font-family:Arial, sans-serif;font-size:14px;border-style:solid;overflow:hidden;word-break:normal;color:black;
         }
      .noborder { 
        border-width: 0px; 
        border-right: #000000;
        border-color: black;
        border-spacing: 0px 0px;
        padding-bottom: 0px;
        padding-top: 0px;
        padding-left: 0px;
        padding-right: 0px;
      }

      .tg .section { background-color: #555 }
      .tg .subsection { background-color: #999 }
  

      .tg th{font-weight:bold; background-color: #111; color: #EEE;}


      .tg tr:nth-child(even) {background-color:#DDD}
      .tg tr:nth-child(odd) {background-color:#FFF}


/*  .title-div   { display: none;}
      .note-div    { display: none;}
      .summary-div { display: none;}*/
    </style>

    <script type="text/javascript">
      function toggleColumn(clazz){
         var els = document.getElementsByClassName(clazz);
         for (aa = els.length-1; aa>=0; aa--){
           if(els[aa].style.display != "inline"){
               els[aa].style.display = "inline";
	   }
	   else{
               els[aa].style.display = "none";
	   }
         }
      }
    </script>
  </head>
  <body>
    <table class="tg">
      <tbody>
      <tr><th>SEC</th><th>SUBSEC</th><th>REQ ID</th><th onClick="toggleColumn('title-div')">APPLIED TEXT</th><th onClick="toggleColumn('note-div')">REQUIREMENT NOTES</th><th>RESULT</th><th onClick="toggleColumn('summary-div')">SUMMARY</th></tr>
""");
    for(sub <- subsections){
      sub.toHtml(System.out, isFirst);
      isFirst=false;
    }
    println(
"""
      </tbody>
    </table>
  </body>
</html>
""");

  }

  /**
    * Handle assurance requirements
    */
  def handleAComponent(comp: Node): Component={
    val ret = new Component(comp\"@id"+"", pp);
    for( group <- comp \ "group" ){
      for(req <- group \ "a-element" ){                            // Go through all the requirements
        ret.addRequirement( handleRequirement(req) );
      }
    }
    ret;
  }

  /**
    * Handle functional requirements
    */
  def handleFComponent(comp: Node): Component={
    val ret = new Component(comp\"@id"+"", pp);
    for(req <- comp \ "f-element" ){
      ret.addRequirement(handleRequirement(req));
    }
    ret;
  }

  /**
    * Finds all descendants of an element that match
    * attribute name/value 
    */
  def getByAtt(e: Elem, att: String, value: String) = {
    //-- Define a temporary function
    def filterAtribute(node: Node, att: String, value: String) = {
      (node \ ("@" + att)).text == value
    }
    //-- Use function to find all elements
    e \\ "_" filter { n=> filterAtribute(n, att, value)}
  }

  /**
    * 
    */
  def handleRequirement(req: Node): Node = {
    val reqId = req \ "@id";
    val results = getByAtt(report, "id", reqId.text);            // Now we look in the report for a requirement
                                                                   // that matches this
    if(results.size == 1){                                         // If it's
      results.head;
    }
    else if(results.size != 0){
      throw new RuntimeException("Non-unique requirements detected for id: " + reqId);
    }
    else{
      null;
    }
  }
}


/**
  * 
  */
object NodeHtmlizer{
  var out: PrintWriter = null;

  def printCCElement(elem:Elem):String={
    val ret = new StringBuffer();
    elem.label match {
      case "selectables" => {
        ret.append("<ul>")
        for(selectable<-elem\"selectable"){
          ret.append("<li>").append(selectable.text).append("</li>");
        }
        ret+"</ul>";
      }
      case "selection" => {
        "<i>"+elem.text + "</i>";
      }
      case "assignable" | "assignment" => {
        "<i>"+elem.text + "</i>";
      }
      case "abbr" | "linkref" => {
        if( elem\@"linkend" != "" ){
          " " + elem\@"linkend"+" "
        }
        else{
          elem.text;
        }
      }
      case "note"|"applied-text"|"title" => {
        for(child<-elem.child){
          ret.append(printNode(child));
        }
        ret+""
      }
      case _ =>{
        System.err.println("Unrecognized element: "+elem.label);
        ""
      }
    }
  }
  def printNodeSeq(seq: NodeSeq):String={
    val ret = new StringBuilder();
    for(node <- seq){
      ret.append(printNode(node));
    }
    ret+"";
  }

  def printNode(root:Node):String={
    root match {
      case elem: Elem => {
        val ret = new StringBuffer();
        if( elem.namespace == "http://www.w3.org/1999/xhtml" ){// 
          //-- Put HTML elements back in.
          ret.append("<"+elem.label);                              
          ret.append(elem.attributes)
          ret.append(">");
          for(child <- elem.child ){
            ret.append(printNode(child));
          }
          ret.append("</"+elem.label+">");
          ret+"";
        }
        else{
          printCCElement(elem);
        }

      }
      case text: Text =>{
        (text+"").replaceAll("&", "&amp;").replaceAll("<", "&lt;");
      }
      case attr: Attribute => {
        ""
      }
      case _ => { 
        System.err.println("Unrecognized node: " + root);
        ""
      }
    }

  }
}

object Reporter {
  /**
    * 
    */
  def main(args: Array[String]) {
    if(args.length !=2){
      System.err.println("Usage: <report> <raw-protection-profile-url>");
    }
    else if(args.length == 2){

      NodeHtmlizer.out = new PrintWriter(System.out);
      new ReportMaker(
        XML.load(args(0)),
        XML.load(args(1)),
        new PrintWriter(System.out)
    ).createReport();
    }
  }
}


object ApplicationProtectionProfile{
  /**
    * 
    */
  def findRequirement(root:Elem, reqId:String)={
    root \\ "_" filter attributeValueEquals(reqId)
  }


  /**
    * 
    */
  def attributeValueEquals(value: String)(node: Node) = {
    node.attributes.exists(_.value.text == value)
  }
}