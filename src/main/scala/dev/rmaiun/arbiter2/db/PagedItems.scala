package dev.rmaiun.arbiter2.db

case class PagedItems[T](items: List[T], pageResult: PageResult)
