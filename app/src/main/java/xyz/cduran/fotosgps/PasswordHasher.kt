
package xyz.cduran.fotosgps

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PasswordHasher {

    fun generarHash(contrasena: String): String {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)

        val spec = PBEKeySpec(
            contrasena.toCharArray(),
            salt,
            120_000,
            256
        )

        val hash = SecretKeyFactory
            .getInstance("PBKDF2WithHmacSHA256")
            .generateSecret(spec)
            .encoded

        spec.clearPassword()

        val saltBase64 = Base64.encodeToString(
            salt,
            Base64.NO_WRAP
        )

        val hashBase64 = Base64.encodeToString(
            hash,
            Base64.NO_WRAP
        )

        return "$saltBase64:$hashBase64"
    }

    fun verificar(contrasena: String, hashGuardado: String): Boolean {
        return try {
            val partes = hashGuardado.split(":")
            if (partes.size != 2) return false

            val salt = Base64.decode(partes[0], Base64.NO_WRAP)
            val hashOriginal = Base64.decode(partes[1], Base64.NO_WRAP)

            val spec = PBEKeySpec(
                contrasena.toCharArray(),
                salt,
                120_000,
                256
            )

            val hashIngresado = SecretKeyFactory
                .getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec)
                .encoded

            spec.clearPassword()

            java.security.MessageDigest.isEqual(
                hashOriginal,
                hashIngresado
            )
        } catch (e: Exception) {
            false
        }
    }

}
