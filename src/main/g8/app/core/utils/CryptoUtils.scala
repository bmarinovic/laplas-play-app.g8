package core.utils

import tsec.passwordhashers.PasswordHash
import tsec.passwordhashers.jca.BCrypt

object CryptoUtils {

  def generatePassword(length: Int, useSpecials: Boolean): String = {
    val rand                      = new scala.util.Random(System.currentTimeMillis)
    val lower                     = ('a' to 'z').mkString
    val upper                     = ('A' to 'Z').mkString
    val nums                      = ('0' to '9').mkString
    val specials                  = "!#\$=?@_~"
    val visuallySimilarCharacters = "o0l1lI5s"

    val pool =
      (lower + upper + nums + (if (useSpecials) specials else "")).filterNot(c => visuallySimilarCharacters.contains(c))
    Stream
      .continually((for (_ <- 1 to length; c = pool(rand.nextInt(pool.length))) yield c).mkString)
      .filter(
        pwd =>
          pwd.exists(_.isUpper) &&
            pwd.exists(_.isLower) &&
            pwd.exists(_.isDigit) &&
            (if (useSpecials) pwd.exists(!_.isLetterOrDigit) else true)
      )
      .head
  }

  def createBCryptHash(plaintext: String): String = BCrypt.hashpwUnsafe(plaintext).toString

  def verifyBCryptHash(plaintext: String, hash: String): Boolean =
    BCrypt.checkpwUnsafe(plaintext, PasswordHash[BCrypt](hash))
}
