package dev.rmaiun.soos.db

case class PagedItems[T](items:List[T], pageResult: PageResult)
