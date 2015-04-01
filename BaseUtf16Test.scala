
object BaseUtf16Test {
    import BaseUtf16._

    def main(args: Array[String]): Unit = {
        val bytes = anyBytes()
        printBytes(bytes)

        val encoded = encode(bytes)
        encoded map (_.toShort) foreach print
        println
        val str = new String(encoded)

        println(str)
        val decoded = decode(str.toCharArray)
        printBytes(decoded)

        println("bytes equals: " + java.util.Arrays.equals(bytes, decoded))
    }

    def printBytes(bytes: Array[Byte]): Unit = {
        bytes map { b => "%02x".format(b)} foreach print
        println
    }

    def anyBytes(len: Int = 100) = {
        
        val ran = new java.security.SecureRandom
        val bytes = new Array[Byte](len)
        ran.nextBytes(bytes)
        bytes
    }
}
