package turbulence

import rudiments.*

package lineSeparation:
  import LineSeparation.Action.*
  import LineSeparation.NewlineSeq
  given carriageReturn: LineSeparation(NewlineSeq.Cr, Nl, Skip, Nl, Nl)
  given strictCarriageReturn: LineSeparation(NewlineSeq.Cr, Nl, Lf, NlLf, LfNl)
  given linefeed: LineSeparation(NewlineSeq.Lf, Skip, Nl, Nl, Nl)
  given strictLinefeeds: LineSeparation(NewlineSeq.Lf, Nl, Lf, NlLf, LfNl)
  given carriageReturnLinefeed: LineSeparation(NewlineSeq.CrLf, Skip, Lf, Nl, LfNl)
  given adaptiveLinefeed: LineSeparation(NewlineSeq.Lf, Nl, Nl, Nl, Nl)
  
  given jvm: LineSeparation = System.lineSeparator.nn match
    case "\r\n"    => carriageReturnLinefeed
    case "\r"      => carriageReturn
    case "\n"      => linefeed
    case _: String => adaptiveLinefeed
  
object LineSeparation:
  inline def readByte(inline read: => Byte, next: => Unit, inline mkNewline: => Unit, inline put: Byte => Unit)
                     (lineSeparators: LineSeparation): Unit =
    val action: Action = read match
      case 10 =>
        next
        
        read match
          case 13 => next; lineSeparators.lfcr
          case ch => lineSeparators.lf
      
      case 13 =>
        next
        
        read match
          case 10 => next; lineSeparators.crlf
          case ch => lineSeparators.cr
      
      case ch =>
        put(ch)
        Action.Skip
    
    action match
      case Action.Nl   => mkNewline
      case Action.NlCr => mkNewline; put(13)
      case Action.NlLf => mkNewline; put(10)
      case Action.CrNl => put(13); mkNewline
      case Action.NlNl => mkNewline; mkNewline
      case Action.Cr   => put(13)
      case Action.Lf   => put(10)
      case Action.LfNl => put(10); mkNewline
      case Action.Skip => ()

  enum NewlineSeq:
    case Cr, Lf, CrLf, LfCr

  enum Action:
    case Nl, NlCr, NlLf, LfNl, CrNl, NlNl, Cr, Lf, Skip

@capability
@missingContext(contextMessage(module = "turbulence", typeclass = "LineSeparation",
    suggest = "lineSeparation.adaptiveLinefeed")(
  "lineSeparation.carriageReturn"         -> "end lines with '\\r', as on Mac OS",
  "lineSeparation.linefeed"               -> "end lines with '\\n', as on UNIX and Linux",
  "lineSeparation.carriageReturnLinefeed" -> "end lines with '\\r\\n', as on Windows",
  "lineSeparation.jvm"                    -> "use the current platform's line endings"
))
case class LineSeparation(newline: LineSeparation.NewlineSeq, cr: LineSeparation.Action,
                              lf: LineSeparation.Action, crlf: LineSeparation.Action,
                              lfcr: LineSeparation.Action):
  def newlineBytes = newline match
    case LineSeparation.NewlineSeq.Cr   => Bytes(13)
    case LineSeparation.NewlineSeq.Lf   => Bytes(10)
    case LineSeparation.NewlineSeq.CrLf => Bytes(13, 10)
    case LineSeparation.NewlineSeq.LfCr => Bytes(10, 13)