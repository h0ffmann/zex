package me.hoffmann.zex.tracing

import java.util.concurrent.TimeUnit

import cats.Monoid
import cats.data.WriterT
import cats.effect.{Clock, IO, Timer}
import io.appium.java_client.MobileElement

import scala.concurrent.ExecutionContext.global
import scala.concurrent.{ExecutionContext, Future}

object TracingMonoid {

  implicit val timer: Timer[IO] = IO.timer(global)

  case class Position(x: Int, y: Int)

  case class Execution(id: Int,
                       label: String,
                       tookInMillis: Long,
                       pos: Position)

  case class Tracings(data: List[Execution])

  type TaskTraced[A] = WriterT[IO, Tracings, A]
  type FutureTraced[A] = WriterT[Future, Tracings, A]

  implicit val timingsMonoid: Monoid[Tracings] = new Monoid[Tracings] {
    def empty: Tracings = Tracings(List.empty)

    def combine(x: Tracings, y: Tracings): Tracings = Tracings(x.data ++ y.data)
  }

  implicit class IOOps(task: IO[MobileElement]) {
    def traced(id: Int, key: String)(implicit clock: Clock[IO]): TaskTraced[MobileElement] =
      WriterT {
        for {
          startTime <- clock.monotonic(TimeUnit.MILLISECONDS)
          result <- task
          endTime <- clock.monotonic(TimeUnit.MILLISECONDS)
        } yield
          (Tracings(List(Execution(
            id,
            key,
            endTime - startTime,
            Position(result.getCenter.x, result.getCenter.y)))), result)
      }

    def untraced: TaskTraced[MobileElement] =
      WriterT(task.map((Monoid[Tracings].empty, _)))
  }

  implicit class FutureOps(task: Future[MobileElement]) {
    def traced(id: Int, key: String)(implicit ec: ExecutionContext): FutureTraced[MobileElement] =
      WriterT {
        for {
          _ <- Future.unit
          startTime = System.currentTimeMillis()
          result <- task
          endTime = System.currentTimeMillis()
        } yield
          Tracings(
            List(
              Execution(
                id,
                key,
                endTime - startTime,
                Position(result.getCenter.x, result.getCenter.y))
            )
          ) -> result
      }

    def untraced(implicit ec: ExecutionContext): FutureTraced[MobileElement] =
      WriterT(task.map((Monoid[Tracings].empty, _)))
  }

}
