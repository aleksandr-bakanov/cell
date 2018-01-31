package bav.cellandroidclient.engine

data class Hex(val q: Int, val r: Int, val s: Int) {

  init {
    assert(q + r + s == 0)
  }

}