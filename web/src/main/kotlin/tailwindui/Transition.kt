@file:JsModule("@tailwindui/react")
@file:JsNonModule

package tailwindui

import react.RClass
import react.RProps

@JsName("Transition")
external val Transition: RClass<TransitionProps>

external interface TransitionProps : RProps {
    var show: Boolean
    var enter: String
    var enterFrom: String
    var enterTo: String
    var leave: String
    var leaveFrom: String
    var leaveTo: String
    var `as`: String
    var className: String
}
