object Main {

  import scala.concurrent._
  import ExecutionContext.Implicits.global

  val R = 10
  val S = 42

  @inline
  def sfib(n: Int): Int = if (n <= 1) 1 else sfib(n - 1) + sfib(n - 2)

  def main(args: Array[String]): Unit = {
    val th = args(1).toInt
    val time = args(0) match {
      case "NB" =>
        bench {
          def fib(n: Int): Future[Int] =
            if (n < th) Future(sfib(n))
            else (Future {
              val f1 = fib(n - 1)
              val f2 = fib(n - 2)
              for (x1 <- f1; x2 <- f2) yield x1 + x2
            }).flatten
          Await.ready(fib(S), duration.Duration.Inf)
        }
      case "NB2" =>
        bench {
          def fib(n: Int): Future[Int] =
            if (n < th) Future(sfib(n))
            else {
              val p = Promise[Int]()
              Future {
                val f1 = fib(n - 1)
                val f2 = fib(n - 2)
                for (x1 <- f1; x2 <- f2) p.success(x1 + x2)
              }
              p.future
            }
          Await.ready(fib(S), duration.Duration.Inf)
        }
      case "BL" =>
        bench {
          def fib(n: Int): Future[Int] =
            if (n < th) Future(sfib(n))
            else Future {
              val f1 = fib(n - 1)
              val f2 = fib(n - 2)
              val x1 = Await.result(f1, duration.Duration.Inf)
              val x2 = Await.result(f2, duration.Duration.Inf)
              x1 + x2
            }
          Await.ready(fib(S), duration.Duration.Inf)
        }
      case "NBRP" =>
        bench {
          var f = Future(())
          var i = 0
          while(i < th) {
            f = (for(_ <- f) yield Future(())).flatten
            i += 1
          }
          Await.ready(f, duration.Duration.Inf)
        }
      case "BLRP" =>
        bench {
          var i = 0
          while(i < th) {
            Await.ready(Future(()), duration.Duration.Inf)
            i += 1
          }
        }
    }
    printf("%s\t%d\t%.6f\n", args(0), th, time)
  }

  @inline
  def bench(run: => Unit): Double = {
    val a = new Array[Long](2 * R)
    for (i <- 0 until 2 * R) {
      val st = System.nanoTime()
      run
      val en = System.nanoTime()
      a(i) = en - st
      println(a(i))
      System.gc()
      Thread.sleep(100)
    }
    a.drop(R).sum * 1e-9 / R
  }
}
