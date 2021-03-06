package info.blockchain.balance

import org.amshove.kluent.`should be`
import org.amshove.kluent.`should equal`
import org.junit.Test

class ExchangeRateTest {

    @Test
    fun `crypto to crypto`() {
        ExchangeRate.CryptoToCrypto(CryptoCurrency.BTC, CryptoCurrency.BCH, 20.toBigDecimal())
            .applyRate(CryptoValue.bitcoinFromMajor(10)) `should equal` CryptoValue.bitcoinCashFromMajor(200)
    }

    @Test
    fun `crypto to fiat`() {
        ExchangeRate.CryptoToFiat(CryptoCurrency.BTC, "USD", 20.toBigDecimal())
            .applyRate(CryptoValue.bitcoinFromMajor(10)) `should equal` 200.usd()
    }

    @Test
    fun `fiat to crypto`() {
        ExchangeRate.FiatToCrypto("USD", CryptoCurrency.BTC, 20.toBigDecimal())
            .applyRate(10.usd()) `should equal` CryptoValue.bitcoinFromMajor(200)
    }

    @Test
    fun `crypto to crypto - from miss match`() {
        ExchangeRate.CryptoToCrypto(CryptoCurrency.BCH, CryptoCurrency.BCH, 20.toBigDecimal())
            .applyRate(CryptoValue.bitcoinFromMajor(10)) `should equal` null
    }

    @Test
    fun `crypto to fiat - from miss match`() {
        ExchangeRate.CryptoToFiat(CryptoCurrency.BTC, "USD", 20.toBigDecimal())
            .applyRate(CryptoValue.etherFromMajor(10)) `should equal` null
    }

    @Test
    fun `fiat to crypto - from miss match`() {
        ExchangeRate.FiatToCrypto("GBP", CryptoCurrency.BTC, 20.toBigDecimal())
            .applyRate(10.usd()) `should equal` null
    }

    @Test
    fun `crypto to crypto - multiply`() {
        val rate = ExchangeRate.CryptoToCrypto(CryptoCurrency.BTC, CryptoCurrency.BCH, 20.toBigDecimal())
        val cryptoValue = CryptoValue.bitcoinFromMajor(10)
        cryptoValue * rate `should equal` CryptoValue.bitcoinCashFromMajor(200)
    }

    @Test
    fun `crypto to fiat - multiply`() {
        val rate: ExchangeRate.CryptoToFiat = ExchangeRate.CryptoToFiat(CryptoCurrency.BTC, "USD", 20.toBigDecimal())
        val cryptoValue = CryptoValue.bitcoinFromMajor(10)

        cryptoValue * rate `should equal` 200.usd()
    }

    @Test
    fun `fiat to crypto - multiply`() {
        val rate = ExchangeRate.FiatToCrypto("USD", CryptoCurrency.BTC, 20.toBigDecimal())

        10.usd() * rate `should equal` CryptoValue.bitcoinFromMajor(200)
    }

    @Test
    fun `crypto to fiat - inverse`() {
        ExchangeRate.CryptoToFiat(CryptoCurrency.BTC, "USD", 20.toBigDecimal()).inverse()
            .applyRate(200.usd()) `should equal` CryptoValue.bitcoinFromMajor(10)
    }

    @Test
    fun `crypto to fiat - divide`() {
        200.usd() / ExchangeRate.CryptoToFiat(
            CryptoCurrency.BTC,
            "USD",
            20.toBigDecimal()
        ) `should equal` CryptoValue.bitcoinFromMajor(10)
    }

    @Test
    fun `fiat to crypto - inverse`() {
        ExchangeRate.FiatToCrypto("USD", CryptoCurrency.BTC, 20.toBigDecimal()).inverse()
            .applyRate(CryptoValue.bitcoinFromMajor(200)) `should equal` 10.usd()
    }

    @Test
    fun `fiat to crypto - divide`() {
        CryptoValue.bitcoinFromMajor(200) / ExchangeRate.FiatToCrypto(
            "USD",
            CryptoCurrency.BTC,
            20.toBigDecimal()
        ) `should equal` 10.usd()
    }

    @Test
    fun `crypto to crypto - inverse`() {
        ExchangeRate.CryptoToCrypto(CryptoCurrency.BTC, CryptoCurrency.BCH, 20.toBigDecimal()).inverse()
            .apply {
                from `should be` CryptoCurrency.BCH
                to `should be` CryptoCurrency.BTC
                rate `should equal` 0.05.toBigDecimal()
            }
    }

    @Test
    fun `crypto to crypto - divide`() {
        val rate = ExchangeRate.CryptoToCrypto(CryptoCurrency.BCH, CryptoCurrency.BTC, 20.toBigDecimal())
        val cryptoValue = CryptoValue.bitcoinFromMajor(20)
        cryptoValue / rate `should equal` CryptoValue.bitcoinCashFromMajor(1)
    }
}
