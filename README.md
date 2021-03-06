# app-vetting 
![Build](https://github.com/commoncriteria/app-vetting/workflows/Build/badge.svg)
[![GitHub issues Open](https://img.shields.io/github/issues/commoncriteria/app-vetting.svg?maxAge=2592000)](https://github.com/commoncriteria/app-vetting/issues) 
![license](https://img.shields.io/badge/license-Unlicensed-blue.svg)

Project to standardize results that undergo evaluation (formal or informal) against NIAP's [Protection Profile for Application Software](https://github.com/commoncriteria/application)

## File/Directory descriptions

* _bin_: Directory of exectuables
  * _app-vetting-full-report_: Executable to generate a full html report. Requires bash, java, and scala.
  * _app-vetting-quick-check_: Executable to generate an html report that specifies which mandatory requirements were statisfied and which were not. The mandatory requirements are specified in _conf/mandatory_requirements.xml_.
  * _app-vetting-full-report-xsl_: The previous executable to generate a full html report. Requires bash and xsltproc.
* _conf/mandatory-requirements.xml_ : Simple list that defines mandatory requirements
* _report-to-html/src/scala_: Scala code used by _app-vetting-full-report
* _xsl_: Directory containing XSL transforms
  * _requirement-checker.xsl_: XSL Transform that checks an AppVetting.xml document against the mandatory requirements defined in _conf/mandatory-requirements.xml_ and generates a short report summarizing the results.
  * _results2vettingreport.xsl_: XSL Transform used by _bin/app-vetting-full-report-xsl_.
* _README.md_: me
* _results-example.xml_: An example of a report
* _vetting-output.rng_: A RelaxNG XML Schema that defines the structure of an AppVetting XMl report.

## Resources
* http://relaxng.org/ : RelaxNG resource
* https://www.niap-ccevs.org/Profile/Info.cfm?id=394 : Application Software Protection Profile v1.2
* http://www.scala-lang.org/ : Scala programing language resource

## Links 
* [National Information Assurance Partnership (NIAP)](https://www.niap-ccevs.org/)
* [Common Criteria Portal](https://www.commoncriteriaportal.org/)

## License

See [License](./LICENSE)

