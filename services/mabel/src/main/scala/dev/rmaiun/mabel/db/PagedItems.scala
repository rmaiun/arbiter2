package dev.rmaiun.mabel.db

case class PagedItems[T](items: List[T], pageResult: PageResult)
