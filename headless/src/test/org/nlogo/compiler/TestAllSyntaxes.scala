// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler

// We have this because in the future we might change how primitives specify their syntax, or how
// the syntaxes are stored.  Having all of the syntaxes here in a simple textual format guards
// against regressions.  (It also means new primitives have to be added here at the same time they
// are added to tokens.txt.) - ST 12/5/09

import org.scalatest.FunSuite
import org.nlogo.nvm.Instruction

class TestAllSyntaxes extends FunSuite {
  def shorten(name: String) =
    Class.forName(name).getSimpleName
  def instruction(name: String) =
    Class.forName(name).newInstance().asInstanceOf[Instruction]
  def entry(name: String) =
    shorten(name) + " " + instruction(name).syntax.dump
  def doTest(classNames: Set[String], expected: String) {
    expectResult(expected)(
      classNames.toSeq.sortBy(shorten).map(entry).mkString("\n"))
  }
  val c = Compiler.TokenMapper2D.allCommandClassNames
  val r = Compiler.TokenMapper2D.allReporterClassNames
  test("commands") { doTest(c, COMMANDS) }
  test("reporters") { doTest(r, REPORTERS) }
  val REPORTERS = """|_abs number,number,OTPL,null,10,1,1
                     |_acos number,number,OTPL,null,10,1,1
                     |_all agentset/TRUE/FALSE block,TRUE/FALSE,OTPL,?,10,2,2
                     |_and TRUE/FALSE,TRUE/FALSE,TRUE/FALSE,OTPL,null,4,1,1
                     |_any agentset,TRUE/FALSE,OTPL,null,10,1,1
                     |_approximatehsb number/number/number,number,OTPL,null,10,3,3
                     |_approximatergb number/number/number,number,OTPL,null,10,3,3
                     |_asin number,number,OTPL,null,10,1,1
                     |_atan number/number,number,OTPL,null,10,2,2
                     |_atpoints turtle agentset or patch agentset,list,agentset,OTPL,null,12,1,1
                     |_autoplot ,TRUE/FALSE,OTPL,null,10,0,0
                     |_basecolors ,list,OTPL,null,10,0,0
                     |_behaviorspacerunnumber ,number,OTPL,null,10,0,0
                     |_boom ,anything,OTPL,null,10,0,0
                     |_bothends ,agentset,---L,null,10,0,0
                     |_butfirst string or list,string or list,OTPL,null,10,1,1
                     |_butlast string or list,string or list,OTPL,null,10,1,1
                     |_canmove number,TRUE/FALSE,-T--,null,10,1,1
                     |_ceil number,number,OTPL,null,10,1,1
                     |_checksyntax string,string,OTPL,null,10,1,1
                     |_cos number,number,OTPL,null,10,1,1
                     |_count agentset,number,OTPL,null,10,1,1
                     |_dateandtime ,string,OTPL,null,10,0,0
                     |_distance turtle or patch,number,-TP-,null,10,1,1
                     |_distancenowrap turtle or patch,number,-TP-,null,10,1,1
                     |_distancexy number/number,number,-TP-,null,10,2,2
                     |_distancexynowrap number/number,number,-TP-,null,10,2,2
                     |_div number,number,number,OTPL,null,8,1,1
                     |_dump ,string,O---,null,10,0,0
                     |_dump1 ,string,OTPL,null,10,0,0
                     |_dumpextensionprims ,string,OTPL,null,10,0,0
                     |_dumpextensions ,string,OTPL,null,10,0,0
                     |_dx ,number,-T--,null,10,0,0
                     |_dy ,number,-T--,null,10,0,0
                     |_empty string or list,TRUE/FALSE,OTPL,null,10,1,1
                     |_end1 ,turtle,---L,null,10,0,0
                     |_end2 ,turtle,---L,null,10,0,0
                     |_equal anything,anything,TRUE/FALSE,OTPL,null,5,1,1
                     |_errormessage ,string,OTPL,null,10,0,0
                     |_exp number,number,OTPL,null,10,1,1
                     |_extracthsb number or list,list,OTPL,null,10,1,1
                     |_extractrgb number,list,OTPL,null,10,1,1
                     |_fileatend ,TRUE/FALSE,OTPL,null,10,0,0
                     |_fileexists string,TRUE/FALSE,OTPL,null,10,1,1
                     |_fileread ,number or TRUE/FALSE or string or list or NOBODY,OTPL,null,10,0,0
                     |_filereadchars number,string,OTPL,null,10,1,1
                     |_filereadline ,string,OTPL,null,10,0,0
                     |_filter reporter task/list,list,OTPL,null,10,2,2
                     |_first string or list,anything,OTPL,null,10,1,1
                     |_floor number,number,OTPL,null,10,1,1
                     |_fput anything/list,list,OTPL,null,10,2,2
                     |_greaterorequal agent or number or string,agent or number or string,TRUE/FALSE,OTPL,null,6,1,1
                     |_greaterthan agent or number or string,agent or number or string,TRUE/FALSE,OTPL,null,6,1,1
                     |_hsb number/number/number,list,OTPL,null,10,3,3
                     |_ifelsevalue TRUE/FALSE/reporter block/reporter block,anything,OTPL,null,10,3,3
                     |_incone turtle agentset or patch agentset,number/number,turtle agentset or patch agentset,OTPL,-T--,12,2,2
                     |_inconenowrap agentset,number/number,agentset,OTPL,-T--,12,2,2
                     |_inlinkfrom agent,agent,-T--,null,10,1,1
                     |_inlinkneighbor agent,TRUE/FALSE,-T--,null,10,1,1
                     |_inlinkneighbors ,agentset,-T--,null,10,0,0
                     |_inradius turtle agentset or patch agentset,number,turtle agentset or patch agentset,-TP-,null,12,1,1
                     |_inradiusnowrap agentset,number,agentset,-TP-,null,12,1,1
                     |_int number,number,OTPL,null,10,1,1
                     |_isagent anything,TRUE/FALSE,OTPL,null,10,1,1
                     |_isagentset anything,TRUE/FALSE,OTPL,null,10,1,1
                     |_isboolean anything,TRUE/FALSE,OTPL,null,10,1,1
                     |_iscommandtask anything,TRUE/FALSE,OTPL,null,10,1,1
                     |_isdirectedlink anything,TRUE/FALSE,OTPL,null,10,1,1
                     |_islink anything,TRUE/FALSE,OTPL,null,10,1,1
                     |_islinkset anything,TRUE/FALSE,OTPL,null,10,1,1
                     |_islist anything,TRUE/FALSE,OTPL,null,10,1,1
                     |_isnumber anything,TRUE/FALSE,OTPL,null,10,1,1
                     |_ispatch anything,TRUE/FALSE,OTPL,null,10,1,1
                     |_ispatchset anything,TRUE/FALSE,OTPL,null,10,1,1
                     |_isreportertask anything,TRUE/FALSE,OTPL,null,10,1,1
                     |_isstring anything,TRUE/FALSE,OTPL,null,10,1,1
                     |_isturtle anything,TRUE/FALSE,OTPL,null,10,1,1
                     |_isturtleset anything,TRUE/FALSE,OTPL,null,10,1,1
                     |_isundirectedlink anything,TRUE/FALSE,OTPL,null,10,1,1
                     |_item number/string or list,anything,OTPL,null,10,2,2
                     |_last string or list,anything,OTPL,null,10,1,1
                     |_length string or list,number,OTPL,null,10,1,1
                     |_lessorequal agent or number or string,agent or number or string,TRUE/FALSE,OTPL,null,6,1,1
                     |_lessthan agent or number or string,agent or number or string,TRUE/FALSE,OTPL,null,6,1,1
                     |_link number/number,link,OTPL,null,10,2,2
                     |_linkheading ,number,---L,null,10,0,0
                     |_linklength ,number,---L,null,10,0,0
                     |_linkneighbor agent,TRUE/FALSE,-T--,null,10,1,1
                     |_linkneighbors ,agentset,-T--,null,10,0,0
                     |_links ,link agentset,OTPL,null,10,0,0
                     |_linkset list or link agentset or link,link agentset,OTPL,null,10,1,0
                     |_linkshapes ,list,OTPL,null,10,0,0
                     |_linkwith agent,link,-T--,null,10,1,1
                     |_list anything,list,OTPL,null,10,2,0
                     |_ln number,number,OTPL,null,10,1,1
                     |_log number/number,number,OTPL,null,10,2,2
                     |_lput anything/list,list,OTPL,null,10,2,2
                     |_map reporter task/list,list,OTPL,null,10,2,2
                     |_max list,number,OTPL,null,10,1,1
                     |_maxnof number/agentset/number block,agentset,OTPL,?,10,3,3
                     |_maxoneof agentset/number block,agent,OTPL,?,10,2,2
                     |_maxpxcor ,number,OTPL,null,10,0,0
                     |_maxpycor ,number,OTPL,null,10,0,0
                     |_mean list,number,OTPL,null,10,1,1
                     |_median list,number,OTPL,null,10,1,1
                     |_member anything/string or list or agentset,TRUE/FALSE,OTPL,null,10,2,2
                     |_min list,number,OTPL,null,10,1,1
                     |_minnof number/agentset/number block,agentset,OTPL,?,10,3,3
                     |_minoneof agentset/number block,agent,OTPL,?,10,2,2
                     |_minpxcor ,number,OTPL,null,10,0,0
                     |_minpycor ,number,OTPL,null,10,0,0
                     |_minus number,number,number,OTPL,null,7,1,1
                     |_mod number,number,number,OTPL,null,8,1,1
                     |_modes list,list,OTPL,null,10,1,1
                     |_mousedown ,TRUE/FALSE,OTPL,null,10,0,0
                     |_mouseinside ,TRUE/FALSE,OTPL,null,10,0,0
                     |_mousexcor ,number,OTPL,null,10,0,0
                     |_mouseycor ,number,OTPL,null,10,0,0
                     |_mult number,number,number,OTPL,null,8,1,1
                     |_myinlinks ,link agentset,-T--,null,10,0,0
                     |_mylinks ,link agentset,-T--,null,10,0,0
                     |_myoutlinks ,link agentset,-T--,null,10,0,0
                     |_myself ,agent,-TPL,null,10,0,0
                     |_nanotime ,number,OTPL,null,10,0,0
                     |_neighbors ,patch agentset,-TP-,null,10,0,0
                     |_neighbors4 ,patch agentset,-TP-,null,10,0,0
                     |_netlogoapplet ,TRUE/FALSE,OTPL,null,10,0,0
                     |_netlogoversion ,string,OTPL,null,10,0,0
                     |_newseed ,number,OTPL,null,10,0,0
                     |_nof number/list or agentset,list or agentset,OTPL,null,10,2,2
                     |_nolinks ,link agentset,OTPL,null,10,0,0
                     |_nopatches ,patch agentset,OTPL,null,10,0,0
                     |_not TRUE/FALSE,TRUE/FALSE,OTPL,null,10,1,1
                     |_notequal anything,anything,TRUE/FALSE,OTPL,null,5,1,1
                     |_noturtles ,turtle agentset,OTPL,null,10,0,0
                     |_nvalues number/reporter task,list,OTPL,null,10,2,2
                     |_of reporter block,agent or agentset,anything,OTPL,?,11,1,1 [RIGHT ASSOCIATIVE]
                     |_oneof list or agentset,anything,OTPL,null,10,1,1
                     |_or TRUE/FALSE,TRUE/FALSE,TRUE/FALSE,OTPL,null,4,1,1
                     |_other agentset,agentset,OTPL,null,10,1,1
                     |_otherend ,agent,-T-L,null,10,0,0
                     |_outlinkneighbor agent,TRUE/FALSE,-T--,null,10,1,1
                     |_outlinkneighbors ,agentset,-T--,null,10,0,0
                     |_outlinkto agent,agent,-T--,null,10,1,1
                     |_patch number/number,patch,OTPL,null,10,2,2
                     |_patchahead number,patch,-T--,null,10,1,1
                     |_patchat number/number,patch,-TP-,null,10,2,2
                     |_patchatheadinganddistance number/number,patch,-TP-,null,10,2,2
                     |_patchcol number,patch agentset,OTPL,null,10,1,1
                     |_patches ,patch agentset,OTPL,null,10,0,0
                     |_patchhere ,patch,-T--,null,10,0,0
                     |_patchleftandahead number/number,patch,-T--,null,10,2,2
                     |_patchrightandahead number/number,patch,-T--,null,10,2,2
                     |_patchrow number,patch agentset,OTPL,null,10,1,1
                     |_patchset list or patch agentset or patch,patch agentset,OTPL,null,10,1,0
                     |_patchsize ,number,OTPL,null,10,0,0
                     |_plotname ,string,OTPL,null,10,0,0
                     |_plotpenexists string,TRUE/FALSE,OTPL,null,10,1,1
                     |_plotxmax ,number,OTPL,null,10,0,0
                     |_plotxmin ,number,OTPL,null,10,0,0
                     |_plotymax ,number,OTPL,null,10,0,0
                     |_plotymin ,number,OTPL,null,10,0,0
                     |_plus number,number,number,OTPL,null,7,1,1
                     |_position anything/string or list,number or TRUE/FALSE,OTPL,null,10,2,2
                     |_pow number,number,number,OTPL,null,9,1,1
                     |_precision number/number,number,OTPL,null,10,2,2
                     |_processors ,number,OTPL,null,10,0,0
                     |_random number,number,OTPL,null,10,1,1
                     |_randomexponential number,number,OTPL,null,10,1,1
                     |_randomfloat number,number,OTPL,null,10,1,1
                     |_randomgamma number/number,number,OTPL,null,10,2,2
                     |_randomnormal number/number,number,OTPL,null,10,2,2
                     |_randompoisson number,number,OTPL,null,10,1,1
                     |_randompxcor ,number,OTPL,null,10,0,0
                     |_randompycor ,number,OTPL,null,10,0,0
                     |_randomstate ,string,OTPL,null,10,0,0
                     |_randomxcor ,number,OTPL,null,10,0,0
                     |_randomycor ,number,OTPL,null,10,0,0
                     |_readfromstring string,number or TRUE/FALSE or string or list or NOBODY,OTPL,null,10,1,1
                     |_reduce reporter task/list,anything,OTPL,null,10,2,2
                     |_remainder number/number,number,OTPL,null,10,2,2
                     |_remove anything/string or list,string or list,OTPL,null,10,2,2
                     |_removeduplicates list,list,OTPL,null,10,1,1
                     |_removeitem number/string or list,string or list,OTPL,null,10,2,2
                     |_replaceitem number/string or list/anything,string or list,OTPL,null,10,3,3
                     |_reverse string or list,string or list,OTPL,null,10,1,1
                     |_rgb number/number/number,list,OTPL,null,10,3,3
                     |_round number,number,OTPL,null,10,1,1
                     |_runresult string or reporter task/anything,anything,OTPL,null,10,1,1
                     |_scalecolor number/number/number/number,number,OTPL,null,10,4,4
                     |_self ,agent,-TPL,null,10,0,0
                     |_sentence anything,list,OTPL,null,10,2,0
                     |_shadeof number/number,TRUE/FALSE,OTPL,null,10,2,2
                     |_shapes ,list,OTPL,null,10,0,0
                     |_shuffle list,list,OTPL,null,10,1,1
                     |_sin number,number,OTPL,null,10,1,1
                     |_sort list or agentset,list,OTPL,null,10,1,1
                     |_sortby reporter task/list or agentset,list,OTPL,?,10,2,2
                     |_sorton reporter block/agentset,list,OTPL,?,10,2,2
                     |_sqrt number,number,OTPL,null,10,1,1
                     |_stacktrace ,string,OTPL,null,10,0,0
                     |_standarddeviation list,number,OTPL,null,10,1,1
                     |_subject ,agent,OTPL,null,10,0,0
                     |_sublist list/number/number,list,OTPL,null,10,3,3
                     |_substring string/number/number,string,OTPL,null,10,3,3
                     |_subtractheadings number/number,number,OTPL,null,10,2,2
                     |_sum list,number,OTPL,null,10,1,1
                     |_tan number,number,OTPL,null,10,1,1
                     |_task reporter task or command task,reporter task or command task,OTPL,null,10,1,1
                     |_ticks ,number,OTPL,null,10,0,0
                     |_timer ,number,OTPL,null,10,0,0
                     |_tostring anything,string,OTPL,null,10,1,1
                     |_towards turtle or patch,number,-TP-,null,10,1,1
                     |_towardsnowrap turtle or patch,number,-TP-,null,10,1,1
                     |_towardsxy number/number,number,-TP-,null,10,2,2
                     |_towardsxynowrap number/number,number,-TP-,null,10,2,2
                     |_turtle number,turtle,OTPL,null,10,1,1
                     |_turtles ,turtle agentset,OTPL,null,10,0,0
                     |_turtlesat number/number,turtle agentset,-TP-,null,10,2,2
                     |_turtleset list or turtle agentset or turtle,turtle agentset,OTPL,null,10,1,0
                     |_turtleshere ,turtle agentset,-TP-,null,10,0,0
                     |_turtleson agent or agentset,turtle agentset,OTPL,null,10,1,1
                     |_userdirectory ,TRUE/FALSE or string,OTPL,null,10,0,0
                     |_userfile ,TRUE/FALSE or string,OTPL,null,10,0,0
                     |_userinput anything,string,OTPL,null,10,1,1
                     |_usernewfile ,TRUE/FALSE or string,OTPL,null,10,0,0
                     |_useroneof anything/list,anything,OTPL,null,10,2,2
                     |_useryesorno anything,TRUE/FALSE,OTPL,null,10,1,1
                     |_variance list,number,OTPL,null,10,1,1
                     |_with agentset,TRUE/FALSE block,agentset,OTPL,?,12,1,1
                     |_withmax agentset,number block,agentset,OTPL,?,12,1,1
                     |_withmin agentset,number block,agentset,OTPL,?,12,1,1
                     |_word anything,string,OTPL,null,10,2,0
                     |_worldheight ,number,OTPL,null,10,0,0
                     |_worldwidth ,number,OTPL,null,10,0,0
                     |_wrapcolor number,number,OTPL,null,10,1,1
                     |_xor TRUE/FALSE,TRUE/FALSE,TRUE/FALSE,OTPL,null,4,1,1""".stripMargin.replaceAll("\r\n", "\n")
  val COMMANDS = """|_ask agent or agentset/command block,OTPL,?,0,2,2 *
                    |_askconcurrent agentset/command block,OTPL,?,0,2,2 *
                    |_autoplotoff ,OTPL,null,0,0,0
                    |_autoploton ,OTPL,null,0,0,0
                    |_beep ,OTPL,null,0,0,0
                    |_bench number/number,O---,null,0,2,2
                    |_bk number,-T--,null,0,1,1
                    |_carefully command block/command block,OTPL,null,0,2,2
                    |_changetopology TRUE/FALSE/TRUE/FALSE,OTPL,null,0,2,2
                    |_clearall ,O---,null,0,0,0 *
                    |_clearallandresetticks ,O---,null,0,0,0 *
                    |_clearallplots ,OTPL,null,0,0,0
                    |_cleardrawing ,O---,null,0,0,0 *
                    |_clearglobals ,O---,null,0,0,0 *
                    |_clearlinks ,O---,null,0,0,0 *
                    |_clearoutput ,OTPL,null,0,0,0
                    |_clearpatches ,O---,null,0,0,0 *
                    |_clearplot ,OTPL,null,0,0,0
                    |_clearticks ,O---,null,0,0,0 *
                    |_clearturtles ,O---,null,0,0,0 *
                    |_createlinkfrom turtle/command block (optional),-T--,---L,0,2,2 *
                    |_createlinksfrom turtle agentset/command block (optional),-T--,---L,0,2,2 *
                    |_createlinksto turtle agentset/command block (optional),-T--,---L,0,2,2 *
                    |_createlinkswith turtle agentset/command block (optional),-T--,---L,0,2,2 *
                    |_createlinkto turtle/command block (optional),-T--,---L,0,2,2 *
                    |_createlinkwith turtle/command block (optional),-T--,---L,0,2,2 *
                    |_createorderedturtles number/command block (optional),O---,-T--,0,2,2 *
                    |_createtemporaryplotpen string,OTPL,null,0,1,1
                    |_createturtles number/command block (optional),O---,-T--,0,2,2 *
                    |_die ,-T-L,null,0,0,0 *
                    |_diffuse variable/number,O---,null,0,2,2 *
                    |_diffuse4 variable/number,O---,null,0,2,2 *
                    |_display ,OTPL,null,0,0,0 *
                    |_done ,OTPL,null,0,0,0
                    |_downhill variable,-T--,null,0,1,1 *
                    |_downhill4 variable,-T--,null,0,1,1 *
                    |_edit ,O---,null,0,0,0
                    |_error anything,OTPL,null,0,1,1
                    |_every number/command block,OTPL,null,0,2,2 *
                    |_experimentstepend ,O---,null,0,0,0
                    |_exportdrawing string,OTPL,null,0,1,1
                    |_exportinterface string,OTPL,null,0,1,1
                    |_exportoutput string,OTPL,null,0,1,1
                    |_exportplot string/string,OTPL,null,0,2,2
                    |_exportplots string,OTPL,null,0,1,1
                    |_exportview string,OTPL,null,0,1,1
                    |_exportworld string,OTPL,null,0,1,1
                    |_face turtle or patch,-T--,null,0,1,1 *
                    |_facenowrap turtle or patch,-T--,null,0,1,1 *
                    |_facexy number/number,-T--,null,0,2,2 *
                    |_facexynowrap number/number,-T--,null,0,2,2 *
                    |_fd number,-T--,null,0,1,1
                    |_fileclose ,OTPL,null,0,0,0
                    |_filecloseall ,OTPL,null,0,0,0
                    |_filedelete string,OTPL,null,0,1,1
                    |_fileflush ,OTPL,null,0,0,0
                    |_fileopen string,OTPL,null,0,1,1
                    |_fileprint anything,OTPL,null,0,1,1
                    |_fileshow anything,OTPL,null,0,1,1
                    |_filetype anything,OTPL,null,0,1,1
                    |_filewrite number or TRUE/FALSE or string or list or NOBODY,OTPL,null,0,1,1
                    |_follow turtle,O---,null,0,1,1 *
                    |_followme ,-T--,null,0,0,0 *
                    |_foreach list/command task,OTPL,null,0,2,2
                    |_foreverbuttonend ,OTPL,null,0,0,0 *
                    |_git string,O---,null,0,1,1
                    |_hatch number/command block (optional),-T--,-T--,0,2,2 *
                    |_hidelink ,---L,null,0,0,0 *
                    |_hideturtle ,-T--,null,0,0,0 *
                    |_histogram list,OTPL,null,0,1,1
                    |_home ,-T--,null,0,0,0 *
                    |_if TRUE/FALSE/command block,OTPL,null,0,2,2
                    |_ifelse TRUE/FALSE/command block/command block,OTPL,null,0,3,3
                    |_ignore anything,OTPL,null,0,1,1
                    |_importdrawing string,O---,null,0,1,1 *
                    |_importpatchcolors string,O---,null,0,1,1 *
                    |_importpcolorsrgb string,O---,null,0,1,1 *
                    |_importworld string,O---,null,0,1,1 *
                    |_inspect agent,OTPL,null,0,1,1
                    |_jump number,-T--,null,0,1,1 *
                    |_layoutcircle list or turtle agentset/number,OTPL,null,0,2,2 *
                    |_layoutradial turtle agentset/link agentset/turtle,OTPL,null,0,3,3 *
                    |_layoutspring turtle agentset/link agentset/number/number/number,OTPL,null,0,5,5 *
                    |_layouttutte turtle agentset/link agentset/number,OTPL,null,0,3,3 *
                    |_left number,-T--,null,0,1,1 *
                    |_let anything/anything,OTPL,null,0,2,2
                    |_linkcode ,---L,null,0,0,0
                    |_loop command block,OTPL,null,0,1,1
                    |_makepreview ,O---,null,0,0,0
                    |_mkdir string,OTPL,null,0,1,1
                    |_moveto turtle or patch,-T--,null,0,1,1 *
                    |_nodisplay ,OTPL,null,0,0,0
                    |_observercode ,O---,null,0,0,0
                    |_outputprint anything,OTPL,null,0,1,1
                    |_outputshow anything,OTPL,null,0,1,1
                    |_outputtype anything,OTPL,null,0,1,1
                    |_outputwrite anything,OTPL,null,0,1,1
                    |_patchcode ,--P-,null,0,0,0
                    |_pendown ,-T--,null,0,0,0 *
                    |_penerase ,-T--,null,0,0,0 *
                    |_penup ,-T--,null,0,0,0 *
                    |_plot number,OTPL,null,0,1,1
                    |_plotpendown ,OTPL,null,0,0,0
                    |_plotpenhide ,OTPL,null,0,0,0
                    |_plotpenreset ,OTPL,null,0,0,0
                    |_plotpenshow ,OTPL,null,0,0,0
                    |_plotpenup ,OTPL,null,0,0,0
                    |_plotxy number/number,OTPL,null,0,2,2
                    |_print anything,OTPL,null,0,1,1
                    |_pwd ,O---,null,0,0,0
                    |_randomseed number,OTPL,null,0,1,1
                    |_reloadextensions ,OTPL,null,0,0,0 *
                    |_repeat number/command block,OTPL,null,0,2,2
                    |_report anything,OTPL,null,0,1,1
                    |_resetperspective ,OTPL,null,0,0,0 *
                    |_resetticks ,O---,null,0,0,0 *
                    |_resettimer ,OTPL,null,0,0,0
                    |_resizeworld number/number/number/number,O---,null,0,4,4 *
                    |_ride turtle,O---,null,0,1,1 *
                    |_rideme ,-T--,null,0,0,0 *
                    |_right number,-T--,null,0,1,1 *
                    |_run string or command task/anything,OTPL,null,0,1,1
                    |_set anything/anything,OTPL,null,0,2,2
                    |_setcurdir string,OTPL,null,0,1,1
                    |_setcurrentplot string,OTPL,null,0,1,1
                    |_setcurrentplotpen string,OTPL,null,0,1,1
                    |_setdefaultshape turtle agentset or link agentset/string,O---,null,0,2,2
                    |_sethistogramnumbars number,OTPL,null,0,1,1
                    |_setlinethickness number,-T--,null,0,1,1 *
                    |_setpatchsize number,O---,null,0,1,1 *
                    |_setplotpencolor number,OTPL,null,0,1,1
                    |_setplotpeninterval number,OTPL,null,0,1,1
                    |_setplotpenmode number,OTPL,null,0,1,1
                    |_setplotxrange number/number,OTPL,null,0,2,2
                    |_setplotyrange number/number,OTPL,null,0,2,2
                    |_setupplots ,OTPL,null,0,0,0
                    |_setxy number/number,-T--,null,0,2,2 *
                    |_show anything,OTPL,null,0,1,1
                    |_showlink ,---L,null,0,0,0 *
                    |_showturtle ,-T--,null,0,0,0 *
                    |_sprout number/command block (optional),--P-,-T--,0,2,2 *
                    |_stamp ,-T-L,null,0,0,0 *
                    |_stamperase ,-T-L,null,0,0,0 *
                    |_stderr anything,OTPL,null,0,1,1
                    |_stdout anything,OTPL,null,0,1,1
                    |_stop ,OTPL,null,0,0,0
                    |_thunkdidfinish ,OTPL,null,0,0,0
                    |_tick ,O---,null,0,0,0 *
                    |_tickadvance number,O---,null,0,1,1 *
                    |_tie ,---L,null,0,0,0 *
                    |_turtlecode ,-T--,null,0,0,0
                    |_type anything,OTPL,null,0,1,1
                    |_untie ,---L,null,0,0,0 *
                    |_updatemonitor anything,O---,null,0,1,1 *
                    |_updateplots ,OTPL,null,0,0,0
                    |_uphill variable,-T--,null,0,1,1 *
                    |_uphill4 variable,-T--,null,0,1,1 *
                    |_usermessage anything,OTPL,null,0,1,1
                    |_wait number,OTPL,null,0,1,1
                    |_watch agent,O---,null,0,1,1 *
                    |_watchme ,-TPL,null,0,0,0 *
                    |_while TRUE/FALSE block/command block,OTPL,null,0,2,2
                    |_withlocalrandomness command block,OTPL,null,0,1,1
                    |_withoutinterruption command block,OTPL,null,0,1,1
                    |_write anything,OTPL,null,0,1,1""".stripMargin.replaceAll("\r\n", "\n")
}
