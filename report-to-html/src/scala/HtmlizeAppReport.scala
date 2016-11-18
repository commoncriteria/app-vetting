import xml.XML._
import scala.xml._
import java.io._
import scala.collection.mutable.ArrayBuffer
import java.util.HashMap


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
        for(child <- elem.child ){
          ret.append(printNode(child));
        }
        ret +""
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

  def printNode(root:Node ):String={
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


      out.println("<td id='"+id+"' class='subsection' rowspan='"+reqCount+"'>"+id+"</td>")
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
        "</td><td class='result'>passed</td><td onClick=\"showOrHide('summary')\" class='summary'><div title='Click to Expand' class='summary-inv'>...</div><div class='summary-div'>"+(req\"activity"\"summary").text +"</div></td>" 
      else 
        "";

    "<td id='"+req\"@id"+"'class='req-id'>"+ req\"@id" +"</td><td onClick=\"showOrHide('title')\" class='apptext'><div title='Click to Expand' class='title-inv'>...</div><div class='title-div'>"+ appliedText +"</div></td><td onClick=\"showOrHide('note')\" class='req-note'><div title='Click to Expand' class='note-inv'>...</div><div class='note-div'>"+ NodeHtmlizer.printNodeSeq(ppReq\"note") +"</div>"+ results;
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
      out.println("<td id='"+id+"' class='section' rowspan='"+reqCount+"'>"+id+"</td>")        // Insert cell
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
      .comment { 
        border-radius: 25px;
        border: 2px solid #73AD21;
        padding: 20px; 
      }
      .tg  {border-collapse:collapse;border-spacing:0;border-color:#aabcfe;}
      .tg td{
         font-family:Arial, sans-serif;
         font-size:14px;
         border-style:solid;
         overflow:hidden;
         word-break:normal;
         color:black;
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


      .title-div, .note-div, .summary-div   { display: none; max-width: 500px;}
      .title-inv, .note-inv, .summary-inv   { display: block; width: 100%; text-align: center; }



    </style>

    <script type="text/javascript">
      function showOrHide(clazz){
         toggleColumn(clazz+"-div", "block");
         toggleColumn(clazz+"-inv", "none");
      }

      function toggleColumn(clazz, nondefault){
         var els = document.getElementsByClassName(clazz);
         for (aa = els.length-1; aa>=0; aa--){
           if(els[aa].style.display != ""){
               els[aa].style.display = "";
	   }
	   else{
               els[aa].style.display = nondefault;
	   }
         }
      }
    </script>
  </head>
  <body>
    <table class="tg">
      <tbody>
      <tr><th>SEC</th><th>SUBSEC</th><th>REQ ID</th><th onClick="showOrHide('title');">APPLIED TEXT</th><th onClick="showOrHide('note')">REQUIREMENT NOTES</th><th>RESULT</th><th onClick="showOrHide('summary')">SUMMARY</th></tr>
""");
    for(sub <- subsections){
      sub.toHtml(System.out, isFirst);
      isFirst=false;
    }
    println(
"""
      </tbody>
    </table>
""");

    println("<br/>");
    println("<h2>Comments</h2>");

    for(comment<-report\\ "comment"){
      println("<h3>");
      for(tag <- comment\"tag"){
        println("<a href='#"+tag.text+"'>"+tag.text+"</a>");
      }
      println("</h3>");
      commentToHtml(comment, System.out);

    }
    println(
"""
  </body>
</html>
""");

  }

  def commentToHtml(comm: Node, out: PrintStream){

    out.println("<table class='comment'><tr><td>" + (comm\"@who")+": "+(comm\"@what")+ "<br/>" + (comm\"@when") + "</td><td>");
    out.println(NodeHtmlizer.printNodeSeq(comm\"text"));
    out.println("</td></tr>");
    for(reply <- comm\"reply" ){
      out.println("<tr><td></td><td>");
      commentToHtml(reply, out);
      out.println("</td></tr>");
    }

    out.println("</table>");


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


