package info.blockchain.balance

import org.amshove.kluent.`should be`
import org.amshove.kluent.`should equal`
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.util.Locale

class XlmCryptoValueTest {

    @Before
    fun setup() {
        Locale.setDefault(Locale.US)
    }

    @Test
    fun `ZeroXlm is same instance as from zero`() {
        CryptoValue.ZeroXlm `should be` CryptoValue.zero(CryptoCurrency.XLM)
    }

    @Test
    fun `format zero`() {
        CryptoValue.ZeroXlm
            .toStringWithSymbol() `should equal` "0 XLM"
    }

    @Test
    fun `format 1`() {
        CryptoCurrency.XLM.withMajorValue(BigDecimal.ONE)
            .toStringWithSymbol() `should equal` "1.0 XLM"
    }

    @Test
    fun `create via lumensFromMajor method`() {
        CryptoValue.lumensFromMajor(123.45.toBigDecimal()) `should equal`
            CryptoValue.fromMajor(CryptoCurrency.XLM, 123.45.toBigDecimal())
    }

    @Test
    fun `create via lumensFromStroop method`() {
        CryptoValue.lumensFromStroop(12345678.toBigInteger()) `should equal` 1.2345678.lumens()
    }

    @Test
    fun `create via constructor`() {
        CryptoValue(CryptoCurrency.XLM, 98765432.toBigInteger()) `should equal` 9.8765432.lumens()
    }

    @Test
    fun `format fractions`() {
        0.1.lumens().toStringWithSymbol() `should equal` "0.1 XLM"
        0.01.lumens().toStringWithSymbol() `should equal` "0.01 XLM"
        0.001.lumens().toStringWithSymbol() `should equal` "0.001 XLM"
        0.0001.lumens().toStringWithSymbol() `should equal` "0.0001 XLM"
        0.00001.lumens().toStringWithSymbol() `should equal` "0.00001 XLM"
        0.000001.lumens().toStringWithSymbol() `should equal` "0.000001 XLM"
        0.0000001.lumens().toStringWithSymbol() `should equal` "0.0000001 XLM"
    }

    @Test
    fun `format in French locale`() {
        Locale.setDefault(Locale.FRANCE)
        1234.56789.lumens().toStringWithSymbol() `should equal` "1 234,56789 XLM"
    }
}
