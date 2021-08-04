package dev.rmaiun.datamanager.repositories

import java.sql.SQLException

import cats.Monad
import com.mairo.ukl.errors.UklException.DbException
import com.mairo.ukl.utils.flow.Flow.Flow
import com.mairo.ukl.utils.flow.ResultOps.Result

trait GenericRepository[F[_], T] {
  def listAll: Flow[F, List[T]]

  def getById(id: Long): Flow[F, Option[T]]

  def deleteById(id: Long): Flow[F, Long]

  def clearTable: Flow[F, Int]
}

object GenericRepository {

  implicit class SqlErrorFormer[F[_] : Monad, T](fa: F[Either[SQLException, T]]) {
    def adaptError: F[Result[T]] = {
      Monad[F].map(fa)(e => e.left.map(err => DbException(err)))
    }
  }

}
