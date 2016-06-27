// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler

// We have this because in the future we might change how primitives specify their syntax, or how
// the syntaxes are stored.  Having all of the syntaxes here in a simple textual format guards
// against regressions.  (It also means new primitives have to be added here at the same time they
// are added to tokens.txt.) - ST 12/5/09

import org.scalatest.FunSuite
import org.nlogo.core.Instruction
import org.nlogo.api.NetLogoLegacyDialect

class TestAllSyntaxes extends FunSuite {
  val mapper = NetLogoLegacyDialect.tokenMapper
  def shorten(i: Instruction) =
    i.getClass.getSimpleName
  def entry(i: Instruction) =
    shorten(i) + " " + i.syntax.dump
  def instruction(name: String) =
    (mapper.getCommand(name) orElse mapper.getReporter(name)).get
  def doTest(names: Set[String], expected: String) {
    val actual = names.map(instruction).map(entry).toSeq.sorted.mkString("\n")
    assertResult(expected)(actual)
  }
  val c = mapper.allCommandNames
  val r = mapper.allReporterNames
  test("commands") { doTest(c, COMMANDS) }
  test("reporters") { doTest(r, REPORTERS) }
  val REPORTERS = """|_abs number,number,OTPL,None,10,1,1
                     |_acos number,number,OTPL,None,10,1,1
                     |_all agentset/TRUE/FALSE block,TRUE/FALSE,OTPL,Some(?),10,2,2
                     |_and TRUE/FALSE,TRUE/FALSE,TRUE/FALSE,OTPL,None,4,1,1
                     |_any agentset,TRUE/FALSE,OTPL,None,10,1,1
                     |_approximatehsb number/number/number,number,OTPL,None,10,3,3
                     |_approximatehsbold number/number/number,number,OTPL,None,10,3,3
                     |_approximatergb number/number/number,number,OTPL,None,10,3,3
                     |_asin number,number,OTPL,None,10,1,1
                     |_atan number/number,number,OTPL,None,10,2,2
                     |_atpoints turtle agentset or patch agentset,list,agentset,OTPL,None,12,1,1
                     |_autoplot ,TRUE/FALSE,OTPL,None,10,0,0
                     |_basecolors ,list,OTPL,None,10,0,0
                     |_behaviorspaceexperimentname ,string,OTPL,None,10,0,0
                     |_behaviorspacerunnumber ,number,OTPL,None,10,0,0
                     |_block code block,string,OTPL,None,10,1,1
                     |_boom ,anything,OTPL,None,10,0,0
                     |_bothends ,agentset,---L,None,10,0,0
                     |_butfirst string or list,string or list,OTPL,None,10,1,1
                     |_butlast string or list,string or list,OTPL,None,10,1,1
                     |_canmove number,TRUE/FALSE,-T--,None,10,1,1
                     |_ceil number,number,OTPL,None,10,1,1
                     |_checksum ,string,O---,None,10,0,0
                     |_checksyntax string,string,OTPL,None,10,1,1
                     |_cos number,number,OTPL,None,10,1,1
                     |_count agentset,number,OTPL,None,10,1,1
                     |_dateandtime ,string,OTPL,None,10,0,0
                     |_distance turtle or patch,number,-TP-,None,10,1,1
                     |_distancenowrap turtle or patch,number,-TP-,None,10,1,1
                     |_distancexy number/number,number,-TP-,None,10,2,2
                     |_distancexynowrap number/number,number,-TP-,None,10,2,2
                     |_div number,number,number,OTPL,None,8,1,1
                     |_dump ,string,O---,None,10,0,0
                     |_dump1 ,string,OTPL,None,10,0,0
                     |_dumpextensionprims ,string,OTPL,None,10,0,0
                     |_dumpextensions ,string,OTPL,None,10,0,0
                     |_dx ,number,-T--,None,10,0,0
                     |_dy ,number,-T--,None,10,0,0
                     |_empty string or list,TRUE/FALSE,OTPL,None,10,1,1
                     |_equal anything,anything,TRUE/FALSE,OTPL,None,5,1,1
                     |_errormessage ,string,OTPL,None,10,0,0
                     |_exp number,number,OTPL,None,10,1,1
                     |_extracthsb number or list,list,OTPL,None,10,1,1
                     |_extracthsbold number or list,list,OTPL,None,10,1,1
                     |_extractrgb number,list,OTPL,None,10,1,1
                     |_fileatend ,TRUE/FALSE,OTPL,None,10,0,0
                     |_fileexists string,TRUE/FALSE,OTPL,None,10,1,1
                     |_fileread ,number or TRUE/FALSE or string or list or NOBODY,OTPL,None,10,0,0
                     |_filereadchars number,string,OTPL,None,10,1,1
                     |_filereadline ,string,OTPL,None,10,0,0
                     |_filter reporter task/list,list,OTPL,None,10,2,2
                     |_first string or list,anything,OTPL,None,10,1,1
                     |_floor number,number,OTPL,None,10,1,1
                     |_fput anything/list,list,OTPL,None,10,2,2
                     |_greaterorequal agent or number or string,agent or number or string,TRUE/FALSE,OTPL,None,6,1,1
                     |_greaterthan agent or number or string,agent or number or string,TRUE/FALSE,OTPL,None,6,1,1
                     |_hsb number/number/number,list,OTPL,None,10,3,3
                     |_hsbold number/number/number,list,OTPL,None,10,3,3
                     |_hubnetclientslist ,list,OTPL,None,10,0,0
                     |_hubnetentermessage ,TRUE/FALSE,OTPL,None,10,0,0
                     |_hubnetexitmessage ,TRUE/FALSE,OTPL,None,10,0,0
                     |_hubnetinqsize ,number,OTPL,None,10,0,0
                     |_hubnetmessage ,anything,OTPL,None,10,0,0
                     |_hubnetmessagesource ,string,OTPL,None,10,0,0
                     |_hubnetmessagetag ,string,OTPL,None,10,0,0
                     |_hubnetmessagewaiting ,TRUE/FALSE,OTPL,None,10,0,0
                     |_hubnetoutqsize ,number,OTPL,None,10,0,0
                     |_ifelsevalue TRUE/FALSE/reporter block/reporter block,anything,OTPL,None,10,3,3
                     |_incone turtle agentset or patch agentset,number/number,turtle agentset or patch agentset,-T--,None,12,2,2
                     |_inconenowrap agentset,number/number,agentset,-T--,None,12,2,2
                     |_inlinkfrom agent,agent,-T--,None,10,1,1
                     |_inlinkneighbor agent,TRUE/FALSE,-T--,None,10,1,1
                     |_inlinkneighbors ,agentset,-T--,None,10,0,0
                     |_inradius turtle agentset or patch agentset,number,turtle agentset or patch agentset,-TP-,None,12,1,1
                     |_inradiusnowrap agentset,number,agentset,-TP-,None,12,1,1
                     |_int number,number,OTPL,None,10,1,1
                     |_isagent anything,TRUE/FALSE,OTPL,None,10,1,1
                     |_isagentset anything,TRUE/FALSE,OTPL,None,10,1,1
                     |_isboolean anything,TRUE/FALSE,OTPL,None,10,1,1
                     |_iscommandtask anything,TRUE/FALSE,OTPL,None,10,1,1
                     |_isdirectedlink anything,TRUE/FALSE,OTPL,None,10,1,1
                     |_islink anything,TRUE/FALSE,OTPL,None,10,1,1
                     |_islinkset anything,TRUE/FALSE,OTPL,None,10,1,1
                     |_islist anything,TRUE/FALSE,OTPL,None,10,1,1
                     |_isnumber anything,TRUE/FALSE,OTPL,None,10,1,1
                     |_ispatch anything,TRUE/FALSE,OTPL,None,10,1,1
                     |_ispatchset anything,TRUE/FALSE,OTPL,None,10,1,1
                     |_isreportertask anything,TRUE/FALSE,OTPL,None,10,1,1
                     |_isstring anything,TRUE/FALSE,OTPL,None,10,1,1
                     |_isturtle anything,TRUE/FALSE,OTPL,None,10,1,1
                     |_isturtleset anything,TRUE/FALSE,OTPL,None,10,1,1
                     |_isundirectedlink anything,TRUE/FALSE,OTPL,None,10,1,1
                     |_item number/string or list,anything,OTPL,None,10,2,2
                     |_last string or list,anything,OTPL,None,10,1,1
                     |_length string or list,number,OTPL,None,10,1,1
                     |_lessorequal agent or number or string,agent or number or string,TRUE/FALSE,OTPL,None,6,1,1
                     |_lessthan agent or number or string,agent or number or string,TRUE/FALSE,OTPL,None,6,1,1
                     |_link number/number,link,OTPL,None,10,2,2
                     |_linkheading ,number,---L,None,10,0,0
                     |_linklength ,number,---L,None,10,0,0
                     |_linkneighbor agent,TRUE/FALSE,-T--,None,10,1,1
                     |_linkneighbors ,agentset,-T--,None,10,0,0
                     |_links ,link agentset,OTPL,None,10,0,0
                     |_linkset list or link agentset or link,link agentset,OTPL,None,10,1,0
                     |_linkshapes ,list,OTPL,None,10,0,0
                     |_linkwith agent,link,-T--,None,10,1,1
                     |_list anything,list,OTPL,None,10,2,0
                     |_ln number,number,OTPL,None,10,1,1
                     |_log number/number,number,OTPL,None,10,2,2
                     |_lput anything/list,list,OTPL,None,10,2,2
                     |_map reporter task/list,list,OTPL,None,10,2,2
                     |_max list,number,OTPL,None,10,1,1
                     |_maxnof number/agentset/number block,agentset,OTPL,Some(?),10,3,3
                     |_maxoneof agentset/number block,agent,OTPL,Some(?),10,2,2
                     |_maxpxcor ,number,OTPL,None,10,0,0
                     |_maxpycor ,number,OTPL,None,10,0,0
                     |_mean list,number,OTPL,None,10,1,1
                     |_median list,number,OTPL,None,10,1,1
                     |_member anything/string or list or agentset,TRUE/FALSE,OTPL,None,10,2,2
                     |_min list,number,OTPL,None,10,1,1
                     |_minnof number/agentset/number block,agentset,OTPL,Some(?),10,3,3
                     |_minoneof agentset/number block,agent,OTPL,Some(?),10,2,2
                     |_minpxcor ,number,OTPL,None,10,0,0
                     |_minpycor ,number,OTPL,None,10,0,0
                     |_minus number,number,number,OTPL,None,7,1,1
                     |_mod number,number,number,OTPL,None,8,1,1
                     |_modes list,list,OTPL,None,10,1,1
                     |_monitorprecision anything/number,number,O---,None,10,2,2
                     |_mousedown ,TRUE/FALSE,OTPL,None,10,0,0
                     |_mouseinside ,TRUE/FALSE,OTPL,None,10,0,0
                     |_mousexcor ,number,OTPL,None,10,0,0
                     |_mouseycor ,number,OTPL,None,10,0,0
                     |_moviestatus ,string,OTPL,None,10,0,0
                     |_mult number,number,number,OTPL,None,8,1,1
                     |_myinlinks ,link agentset,-T--,None,10,0,0
                     |_mylinks ,link agentset,-T--,None,10,0,0
                     |_myoutlinks ,link agentset,-T--,None,10,0,0
                     |_myself ,agent,-TPL,None,10,0,0
                     |_nanotime ,number,OTPL,None,10,0,0
                     |_neighbors ,patch agentset,-TP-,None,10,0,0
                     |_neighbors4 ,patch agentset,-TP-,None,10,0,0
                     |_netlogoapplet ,TRUE/FALSE,OTPL,None,10,0,0
                     |_netlogoversion ,string,OTPL,None,10,0,0
                     |_netlogoweb ,TRUE/FALSE,OTPL,None,10,0,0
                     |_newseed ,number,OTPL,None,10,0,0
                     |_nof number/list or agentset,list or agentset,OTPL,None,10,2,2
                     |_nolinks ,link agentset,OTPL,None,10,0,0
                     |_nopatches ,patch agentset,OTPL,None,10,0,0
                     |_not TRUE/FALSE,TRUE/FALSE,OTPL,None,10,1,1
                     |_notequal anything,anything,TRUE/FALSE,OTPL,None,5,1,1
                     |_noturtles ,turtle agentset,OTPL,None,10,0,0
                     |_nvalues number/reporter task,list,OTPL,None,10,2,2
                     |_of reporter block,agent or agentset,anything,OTPL,Some(?),11,1,1 [RIGHT ASSOCIATIVE]
                     |_oneof list or agentset,anything,OTPL,None,10,1,1
                     |_or TRUE/FALSE,TRUE/FALSE,TRUE/FALSE,OTPL,None,4,1,1
                     |_other agentset,agentset,OTPL,None,10,1,1
                     |_otherend ,agent,-T-L,None,10,0,0
                     |_outlinkneighbor agent,TRUE/FALSE,-T--,None,10,1,1
                     |_outlinkneighbors ,agentset,-T--,None,10,0,0
                     |_outlinkto agent,agent,-T--,None,10,1,1
                     |_patch number/number,patch,OTPL,None,10,2,2
                     |_patchahead number,patch,-T--,None,10,1,1
                     |_patchat number/number,patch,-TP-,None,10,2,2
                     |_patchatheadinganddistance number/number,patch,-TP-,None,10,2,2
                     |_patchcol number,patch agentset,OTPL,None,10,1,1
                     |_patches ,patch agentset,OTPL,None,10,0,0
                     |_patchhere ,patch,-T--,None,10,0,0
                     |_patchleftandahead number/number,patch,-T--,None,10,2,2
                     |_patchrightandahead number/number,patch,-T--,None,10,2,2
                     |_patchrow number,patch agentset,OTPL,None,10,1,1
                     |_patchset list or patch agentset or patch,patch agentset,OTPL,None,10,1,0
                     |_patchsize ,number,OTPL,None,10,0,0
                     |_plotname ,string,OTPL,None,10,0,0
                     |_plotpenexists string,TRUE/FALSE,OTPL,None,10,1,1
                     |_plotxmax ,number,OTPL,None,10,0,0
                     |_plotxmin ,number,OTPL,None,10,0,0
                     |_plotymax ,number,OTPL,None,10,0,0
                     |_plotymin ,number,OTPL,None,10,0,0
                     |_plus number,number,number,OTPL,None,7,1,1
                     |_position anything/string or list,number or TRUE/FALSE,OTPL,None,10,2,2
                     |_pow number,number,number,OTPL,None,9,1,1
                     |_precision number/number,number,OTPL,None,10,2,2
                     |_processors ,number,OTPL,None,10,0,0
                     |_random number,number,OTPL,None,10,1,1
                     |_randomexponential number,number,OTPL,None,10,1,1
                     |_randomfloat number,number,OTPL,None,10,1,1
                     |_randomgamma number/number,number,OTPL,None,10,2,2
                     |_randomnormal number/number,number,OTPL,None,10,2,2
                     |_randomorrandomfloat number,number,OTPL,None,10,1,1
                     |_randompoisson number,number,OTPL,None,10,1,1
                     |_randompxcor ,number,OTPL,None,10,0,0
                     |_randompycor ,number,OTPL,None,10,0,0
                     |_randomstate ,string,OTPL,None,10,0,0
                     |_randomxcor ,number,OTPL,None,10,0,0
                     |_randomycor ,number,OTPL,None,10,0,0
                     |_readfromstring string,number or TRUE/FALSE or string or list or NOBODY,OTPL,None,10,1,1
                     |_reduce reporter task/list,anything,OTPL,None,10,2,2
                     |_remainder number/number,number,OTPL,None,10,2,2
                     |_remove anything/string or list,string or list,OTPL,None,10,2,2
                     |_removeduplicates list,list,OTPL,None,10,1,1
                     |_removeitem number/string or list,string or list,OTPL,None,10,2,2
                     |_replaceitem number/string or list/anything,string or list,OTPL,None,10,3,3
                     |_reverse string or list,string or list,OTPL,None,10,1,1
                     |_rgb number/number/number,list,OTPL,None,10,3,3
                     |_round number,number,OTPL,None,10,1,1
                     |_runresult string or reporter task/anything,anything,OTPL,None,10,1,1
                     |_scalecolor number/number/number/number,number,OTPL,None,10,4,4
                     |_self ,agent,-TPL,None,10,0,0
                     |_sentence anything,list,OTPL,None,10,2,0
                     |_shadeof number/number,TRUE/FALSE,OTPL,None,10,2,2
                     |_shapes ,list,OTPL,None,10,0,0
                     |_shuffle list,list,OTPL,None,10,1,1
                     |_sin number,number,OTPL,None,10,1,1
                     |_sort list or agentset,list,OTPL,None,10,1,1
                     |_sortby reporter task/list or agentset,list,OTPL,Some(?),10,2,2
                     |_sorton reporter block/agentset,list,OTPL,Some(?),10,2,2
                     |_sqrt number,number,OTPL,None,10,1,1
                     |_stacktrace ,string,OTPL,None,10,0,0
                     |_standarddeviation list,number,OTPL,None,10,1,1
                     |_subject ,agent,OTPL,None,10,0,0
                     |_sublist list/number/number,list,OTPL,None,10,3,3
                     |_substring string/number/number,string,OTPL,None,10,3,3
                     |_subtractheadings number/number,number,OTPL,None,10,2,2
                     |_sum list,number,OTPL,None,10,1,1
                     |_symbolstring symbol,string,OTPL,None,10,1,1
                     |_tan number,number,OTPL,None,10,1,1
                     |_task reporter task or command task,reporter task or command task,OTPL,None,10,1,1
                     |_ticks ,number,OTPL,None,10,0,0
                     |_timer ,number,OTPL,None,10,0,0
                     |_tostring anything,string,OTPL,None,10,1,1
                     |_towards turtle or patch,number,-TP-,None,10,1,1
                     |_towardsnowrap turtle or patch,number,-TP-,None,10,1,1
                     |_towardsxy number/number,number,-TP-,None,10,2,2
                     |_towardsxynowrap number/number,number,-TP-,None,10,2,2
                     |_turtle number,turtle,OTPL,None,10,1,1
                     |_turtles ,turtle agentset,OTPL,None,10,0,0
                     |_turtlesat number/number,turtle agentset,-TP-,None,10,2,2
                     |_turtleset list or turtle agentset or turtle,turtle agentset,OTPL,None,10,1,0
                     |_turtleshere ,turtle agentset,-TP-,None,10,0,0
                     |_turtleson agent or agentset,turtle agentset,OTPL,None,10,1,1
                     |_userdirectory ,TRUE/FALSE or string,OTPL,None,10,0,0
                     |_userfile ,TRUE/FALSE or string,OTPL,None,10,0,0
                     |_userinput anything,string,OTPL,None,10,1,1
                     |_usernewfile ,TRUE/FALSE or string,OTPL,None,10,0,0
                     |_useroneof anything/list,anything,OTPL,None,10,2,2
                     |_useryesorno anything,TRUE/FALSE,OTPL,None,10,1,1
                     |_variance list,number,OTPL,None,10,1,1
                     |_with agentset,TRUE/FALSE block,agentset,OTPL,Some(?),12,1,1
                     |_withmax agentset,number block,agentset,OTPL,Some(?),12,1,1
                     |_withmin agentset,number block,agentset,OTPL,Some(?),12,1,1
                     |_word anything,string,OTPL,None,10,2,0
                     |_worldheight ,number,OTPL,None,10,0,0
                     |_worldwidth ,number,OTPL,None,10,0,0
                     |_wrapcolor number,number,OTPL,None,10,1,1
                     |_xor TRUE/FALSE,TRUE/FALSE,TRUE/FALSE,OTPL,None,4,1,1""".stripMargin.replaceAll("\r\n", "\n")
  val COMMANDS = """|_ask agent or agentset/command block,OTPL,Some(?),0,2,2
                    |_askconcurrent agentset/command block,OTPL,Some(?),0,2,2
                    |_autoplotoff ,OTPL,None,0,0,0
                    |_autoploton ,OTPL,None,0,0,0
                    |_beep ,OTPL,None,0,0,0
                    |_bench number/number,O---,None,0,2,2
                    |_bk number,-T--,None,0,1,1
                    |_carefully command block/command block,OTPL,None,0,2,2
                    |_changetopology TRUE/FALSE/TRUE/FALSE,OTPL,None,0,2,2
                    |_clearall ,O---,None,0,0,0
                    |_clearallandresetticks ,O---,None,0,0,0
                    |_clearallplots ,OTPL,None,0,0,0
                    |_cleardrawing ,O---,None,0,0,0
                    |_clearglobals ,O---,None,0,0,0
                    |_clearlinks ,O---,None,0,0,0
                    |_clearoutput ,OTPL,None,0,0,0
                    |_clearpatches ,O---,None,0,0,0
                    |_clearplot ,OTPL,None,0,0,0
                    |_clearticks ,O---,None,0,0,0
                    |_clearturtles ,O---,None,0,0,0
                    |_createlinkfrom turtle/command block (optional),-T--,Some(---L),0,2,2
                    |_createlinksfrom turtle agentset/command block (optional),-T--,Some(---L),0,2,2
                    |_createlinksto turtle agentset/command block (optional),-T--,Some(---L),0,2,2
                    |_createlinkswith turtle agentset/command block (optional),-T--,Some(---L),0,2,2
                    |_createlinkto turtle/command block (optional),-T--,Some(---L),0,2,2
                    |_createlinkwith turtle/command block (optional),-T--,Some(---L),0,2,2
                    |_createorderedturtles number/command block (optional),O---,Some(-T--),0,2,2
                    |_createtemporaryplotpen string,OTPL,None,0,1,1
                    |_createturtles number/command block (optional),O---,Some(-T--),0,2,2
                    |_deletelogfiles ,O---,None,0,0,0
                    |_die ,-T-L,None,0,0,0
                    |_diffuse variable/number,O---,None,0,2,2
                    |_diffuse4 variable/number,O---,None,0,2,2
                    |_display ,OTPL,None,0,0,0
                    |_done ,OTPL,None,0,0,0
                    |_downhill variable,-T--,None,0,1,1
                    |_downhill4 variable,-T--,None,0,1,1
                    |_edit ,O---,None,0,0,0
                    |_english ,O---,None,0,0,0
                    |_error anything,OTPL,None,0,1,1
                    |_every number/command block,OTPL,None,0,2,2
                    |_experimentstepend ,O---,None,0,0,0
                    |_exportdrawing string,OTPL,None,0,1,1
                    |_exportinterface string,OTPL,None,0,1,1
                    |_exportoutput string,OTPL,None,0,1,1
                    |_exportplot string/string,OTPL,None,0,2,2
                    |_exportplots string,OTPL,None,0,1,1
                    |_exportview string,OTPL,None,0,1,1
                    |_exportworld string,OTPL,None,0,1,1
                    |_face turtle or patch,OT--,None,0,1,1
                    |_facenowrap turtle or patch,OT--,None,0,1,1
                    |_facexy number/number,OT--,None,0,2,2
                    |_facexynowrap number/number,-T--,None,0,2,2
                    |_fd number,-T--,None,0,1,1
                    |_fileclose ,OTPL,None,0,0,0
                    |_filecloseall ,OTPL,None,0,0,0
                    |_filedelete string,OTPL,None,0,1,1
                    |_fileflush ,OTPL,None,0,0,0
                    |_fileopen string,OTPL,None,0,1,1
                    |_fileprint anything,OTPL,None,0,1,1
                    |_fileshow anything,OTPL,None,0,1,1
                    |_filetype anything,OTPL,None,0,1,1
                    |_filewrite number or TRUE/FALSE or string or list or NOBODY,OTPL,None,0,1,1
                    |_fire ,O---,None,0,0,0
                    |_follow turtle,O---,None,0,1,1
                    |_followme ,-T--,None,0,0,0
                    |_foreach list/command task,OTPL,None,0,2,2
                    |_foreverbuttonend ,OTPL,None,0,0,0
                    |_git string,O---,None,0,1,1
                    |_hatch number/command block (optional),-T--,Some(-T--),0,2,2
                    |_hidelink ,---L,None,0,0,0
                    |_hideturtle ,-T--,None,0,0,0
                    |_histogram list,OTPL,None,0,1,1
                    |_histogramfrom agentset/number block,OTPL,None,0,2,2
                    |_home ,-T--,None,0,0,0
                    |_hubnetbroadcast string/anything,OTPL,None,0,2,2
                    |_hubnetbroadcastclearoutput ,OTPL,None,0,0,0
                    |_hubnetbroadcastmessage anything,OTPL,None,0,1,1
                    |_hubnetbroadcastusermessage anything,OTPL,None,0,1,1
                    |_hubnetclearoverride string/agent or agentset/string,OTPL,Some(?),0,3,3
                    |_hubnetclearoverrides string,OTPL,None,0,1,1
                    |_hubnetclearplot string,OTPL,None,0,1,1
                    |_hubnetcreateclient ,O---,None,0,0,0
                    |_hubnetfetchmessage ,OTPL,None,0,0,0
                    |_hubnetkickallclients ,OTPL,None,0,0,0
                    |_hubnetkickclient string,OTPL,None,0,1,1
                    |_hubnetmakeplotnarrowcast string,OTPL,None,0,1,1
                    |_hubnetplot string/number,OTPL,None,0,2,2
                    |_hubnetplotpendown string,OTPL,None,0,1,1
                    |_hubnetplotpenup string,OTPL,None,0,1,1
                    |_hubnetplotxy string/number/number,OTPL,None,0,3,3
                    |_hubnetreset ,O---,None,0,0,0
                    |_hubnetresetperspective string,OTPL,None,0,1,1
                    |_hubnetroboclient number,O---,None,0,1,1
                    |_hubnetsend string or list/string/anything,OTPL,None,0,3,3
                    |_hubnetsendclearoutput string or list,OTPL,None,0,1,1
                    |_hubnetsendfollow string/agent/number,OTPL,None,0,3,3
                    |_hubnetsendfromlocalclient string/string/anything,OTPL,None,0,3,3
                    |_hubnetsendmessage string or list/anything,OTPL,None,0,2,2
                    |_hubnetsendoverride string/agent or agentset/string/reporter block,OTPL,Some(?),0,4,4
                    |_hubnetsendusermessage string or list/anything,OTPL,None,0,2,2
                    |_hubnetsendwatch string/agent,OTPL,None,0,2,2
                    |_hubnetsetclientinterface string/list,O---,None,0,2,2
                    |_hubnetsethistogramnumbars string/number,OTPL,None,0,2,2
                    |_hubnetsetplotmirroring TRUE/FALSE,OTPL,None,0,1,1
                    |_hubnetsetplotpeninterval string/number,OTPL,None,0,2,2
                    |_hubnetsetplotpenmode string/number,OTPL,None,0,2,2
                    |_hubnetsetviewmirroring TRUE/FALSE,OTPL,None,0,1,1
                    |_hubnetwaitforclients number/number,OTPL,None,0,2,2
                    |_hubnetwaitformessages number/number,OTPL,None,0,2,2
                    |_if TRUE/FALSE/command block,OTPL,None,0,2,2
                    |_ifelse TRUE/FALSE/command block/command block,OTPL,None,0,3,3
                    |_ignore anything,OTPL,None,0,1,1
                    |_importdrawing string,O---,None,0,1,1
                    |_importpatchcolors string,O---,None,0,1,1
                    |_importpcolorsrgb string,O---,None,0,1,1
                    |_importworld string,O---,None,0,1,1
                    |_inspect agent,OTPL,None,0,1,1
                    |_inspectwithradius agent/number,OTPL,None,0,2,2
                    |_jump number,-T--,None,0,1,1
                    |_layoutcircle list or turtle agentset/number,OTPL,None,0,2,2
                    |_layoutradial turtle agentset/link agentset/turtle,OTPL,None,0,3,3
                    |_layoutspring turtle agentset/link agentset/number/number/number,OTPL,None,0,5,5
                    |_layouttutte turtle agentset/link agentset/number,OTPL,None,0,3,3
                    |_left number,-T--,None,0,1,1
                    |_let anything/anything,OTPL,None,0,2,2
                    |_life ,O---,None,0,0,0
                    |_linkcode ,---L,None,0,0,0
                    |_loop command block,OTPL,None,0,1,1
                    |_mkdir string,OTPL,None,0,1,1
                    |_moveto turtle or patch,OT--,None,0,1,1
                    |_moviecancel ,OTPL,None,0,0,0
                    |_movieclose ,OTPL,None,0,0,0
                    |_moviegrabinterface ,OTPL,None,0,0,0
                    |_moviegrabview ,OTPL,None,0,0,0
                    |_moviesetframerate number,OTPL,None,0,1,1
                    |_moviestart string,OTPL,None,0,1,1
                    |_nodisplay ,OTPL,None,0,0,0
                    |_observercode ,O---,None,0,0,0
                    |_outputprint anything,OTPL,None,0,1,1
                    |_outputshow anything,OTPL,None,0,1,1
                    |_outputtype anything,OTPL,None,0,1,1
                    |_outputwrite number or TRUE/FALSE or string or list or NOBODY,OTPL,None,0,1,1
                    |_patchcode ,--P-,None,0,0,0
                    |_pendown ,-T--,None,0,0,0
                    |_penerase ,-T--,None,0,0,0
                    |_penup ,-T--,None,0,0,0
                    |_plot number,OTPL,None,0,1,1
                    |_plotpendown ,OTPL,None,0,0,0
                    |_plotpenhide ,OTPL,None,0,0,0
                    |_plotpenreset ,OTPL,None,0,0,0
                    |_plotpenshow ,OTPL,None,0,0,0
                    |_plotpenup ,OTPL,None,0,0,0
                    |_plotxy number/number,OTPL,None,0,2,2
                    |_print anything,OTPL,None,0,1,1
                    |_pwd ,O---,None,0,0,0
                    |_randomseed number,OTPL,None,0,1,1
                    |_reload ,O---,None,0,0,0
                    |_reloadextensions ,OTPL,None,0,0,0
                    |_repeat number/command block,OTPL,None,0,2,2
                    |_report anything,OTPL,None,0,1,1
                    |_resetperspective ,OTPL,None,0,0,0
                    |_resetticks ,O---,None,0,0,0
                    |_resettimer ,OTPL,None,0,0,0
                    |_resizeworld number/number/number/number,O---,None,0,4,4
                    |_ride turtle,O---,None,0,1,1
                    |_rideme ,-T--,None,0,0,0
                    |_right number,-T--,None,0,1,1
                    |_run string or command task/anything,OTPL,None,0,1,1
                    |_set anything/anything,OTPL,None,0,2,2
                    |_setcurdir string,OTPL,None,0,1,1
                    |_setcurrentplot string,OTPL,None,0,1,1
                    |_setcurrentplotpen string,OTPL,None,0,1,1
                    |_setdefaultshape turtle agentset or link agentset/string,O---,None,0,2,2
                    |_seterrorlocale string/string,O---,None,0,2,2
                    |_sethistogramnumbars number,OTPL,None,0,1,1
                    |_setlinethickness number,-T--,None,0,1,1
                    |_setpatchsize number,O---,None,0,1,1
                    |_setplotpencolor number,OTPL,None,0,1,1
                    |_setplotpeninterval number,OTPL,None,0,1,1
                    |_setplotpenmode number,OTPL,None,0,1,1
                    |_setplotxrange number/number,OTPL,None,0,2,2
                    |_setplotyrange number/number,OTPL,None,0,2,2
                    |_setupplots ,OTPL,None,0,0,0
                    |_setxy number/number,-T--,None,0,2,2
                    |_show anything,OTPL,None,0,1,1
                    |_showlink ,---L,None,0,0,0
                    |_showturtle ,-T--,None,0,0,0
                    |_spanish ,O---,None,0,0,0
                    |_sprout number/command block (optional),--P-,Some(-T--),0,2,2
                    |_stamp ,-T-L,None,0,0,0
                    |_stamperase ,-T-L,None,0,0,0
                    |_stderr anything,OTPL,None,0,1,1
                    |_stdout anything,OTPL,None,0,1,1
                    |_stop ,OTPL,None,0,0,0
                    |_stopinspecting agent,OTPL,None,0,1,1
                    |_stopinspectingdeadagents ,OTPL,None,0,0,0
                    |_thunkdidfinish ,OTPL,None,0,0,0
                    |_tick ,O---,None,0,0,0
                    |_tickadvance number,O---,None,0,1,1
                    |_tie ,---L,None,0,0,0
                    |_turtlecode ,-T--,None,0,0,0
                    |_type anything,OTPL,None,0,1,1
                    |_untie ,---L,None,0,0,0
                    |_updatemonitor anything,O---,None,0,1,1
                    |_updateplots ,OTPL,None,0,0,0
                    |_uphill variable,-T--,None,0,1,1
                    |_uphill4 variable,-T--,None,0,1,1
                    |_usermessage anything,OTPL,None,0,1,1
                    |_wait number,OTPL,None,0,1,1
                    |_watch agent,O---,None,0,1,1
                    |_watchme ,-TPL,None,0,0,0
                    |_while TRUE/FALSE block/command block,OTPL,None,0,2,2
                    |_withlocalrandomness command block,OTPL,None,0,1,1
                    |_withoutinterruption command block,OTPL,None,0,1,1
                    |_write number or TRUE/FALSE or string or list or NOBODY,OTPL,None,0,1,1
                    |_ziplogfiles string,O---,None,0,1,1""".stripMargin.replaceAll("\r\n", "\n")
}
