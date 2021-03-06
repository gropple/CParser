package parsing

import fastparse.core.Parsed
import fastparse.utils.IndexedParserInput

/**
  * Wraps fastparse's results, providing some extra debug info
  */
sealed trait CParseResult[T]

case class CParseSuccess[T](t: T) extends CParseResult[T]

case class CParseFail[T, Elem, Repr](parsed: Parsed[T, Elem, Repr]) extends CParseResult[T] {
  val (err: String, failIndex: Int) = parsed match {
    case Parsed.Failure(x, failIndex: Int, z) =>
      val traced = z.traced
      val input = traced.input.asInstanceOf[IndexedParserInput[Char, String]].data
      val err = if (traced.fullStack.nonEmpty) {
        val last = traced.fullStack.last
        s"At index $failIndex '${input.substring(failIndex, Math.min(input.length, failIndex + 10))}'... did not find expected '${last.parser.toString}'"
      }
      else {
        s"At index $failIndex '${input.substring(failIndex, Math.min(input.length, failIndex + 10))}'... parse error "
      }
      (err, failIndex)
  }

  override def toString: String = err
}

object CParseResult {
  def wrap[T, Elem, Repr](in: Parsed[T, Elem, Repr]): CParseResult[T] = {
    in match {
      case Parsed.Success(x, y)                 => CParseSuccess(x)
      case Parsed.Failure(x, failIndex: Int, z) => CParseFail(in)
    }
  }

  private[parsing] def wrapFailed[T, Elem, Repr](in: Parsed[T, Elem, Repr]): CParseFail[T,Elem,Repr] = {
    in match {
      case Parsed.Failure(x, failIndex: Int, z) => CParseFail(in)
      case _ =>
        assert(false)
        null
    }
  }
}