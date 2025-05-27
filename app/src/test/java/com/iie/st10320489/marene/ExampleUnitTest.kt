// REMOVE THIS:
// import com.google.common.truth.Truth.assertThat
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
//import org.hamcrest.Matchers.equalTo
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertThat(2 + 2, equalTo(4))
    }
}
