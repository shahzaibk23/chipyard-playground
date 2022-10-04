package chipyard.example

import chisel3._
import chisel3.util._
import chisel3.experimental.{IntParam, BaseModule}
import freechips.rocketchip.subsystem.BaseSubSystem
import freechips.rocketchip.config.{Parameters, Field, Config}
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.regmapper.{HasRegMap, RegField}
import freechips.rocketchip.util.{UintIsOneOf}

case class JustReadParams(
    address : BigInt = 0x2000,
    width: Int = 32,
)

case object JustReadKey extends Field[Option[JustReadParams]](None)

class JustReadIO(val w:Int) extends Bundle{
    val outPortToBeRead = Output(UInt(w.W))
}

trait HasJustReadIO extends BaseModule {
    val w: Int
    val io = IO(new JustReadIO(w))
}

class JustReadMMIOChiselModule(val w:Int) extends Module with HasJustReadIO {
    val a = RegInit(230.U(w.W))
    io.outPortToBeRead := a
}

trait JustReadTopIO extends Bundle{
    val isRead = Input(Bool())
}

trait  JustReadTopModule extends HasRegMap{
    val io: JustReadTopIO
    implicit val p: Parameters
    def params: JustReadParams

    val isJustRead = RegInit(UInt(params.width.W))

    val impl = Module(new JustReadMMIOChiselModule(params.width))

    isJustRead := impl.io.outPortToBeRead

    regmap(
        0x00 -> Seq(
            
            RegField(params.width, isJustRead, RegFieldDesc("isJustRead", "isJustRead"))

        ),
    )
}


class JustReadTL(params: JustReadParams, beatByts:Int)(implicit p:Parameters) 
    extends TLRegisterRouter(
        params.address, "justRead", Seq("apaj,justreadit"),beatBytes = beatByts
    )(
        new TLRegBundle(params, _) with JustReadTopIO
    )(
        new TLRegModule(params, _, _) with JustReadTopModule
    )


trait  CanHavePeripheryJustReadModuleImp extends LazyModuleImp {
    val outer: CanHavePeripheryJustRead
}

trait CanHavePeripheryJustRead {
    this: BaseSubSystem =>
        private val portName = "justReadPortName"

        val justRead= p(JustReadKey) match {
            case Some(params) => {
                val justRead = LazyModule(new JustReadTL(params, pbus.beatBytes)(p))
                pbus.toVariableWidthSlave(Some(portName)) { justRead.node }
            }
            case None => None
        }
        
}