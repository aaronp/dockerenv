package dockerenv

class ElasticSearchTest extends BaseElasticSearchSpec {

  "ElasticSearchSpec" should {
    "spin up elasticsearch" in {
      isDockerRunning() shouldBe true
    }
  }
}
