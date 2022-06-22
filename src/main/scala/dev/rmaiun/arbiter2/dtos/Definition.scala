package dev.rmaiun.arbiter2.dtos

import CmdType.{ Internal, Persistence, Query }

case class Definition(cmdType: CmdType, supportCommands: Set[String])

object Definition {
  def persistence(commands: Set[String]): Definition = Definition(Persistence, commands)
  def persistence(command: String): Definition       = Definition(Persistence, Set(command))
  def query(commands: Set[String]): Definition       = Definition(Query, commands)
  def query(command: String): Definition             = Definition(Query, Set(command))
  def internal(commands: Set[String]): Definition    = Definition(Internal, commands)
  def internal(command: String): Definition          = Definition(Internal, Set(command))

}
