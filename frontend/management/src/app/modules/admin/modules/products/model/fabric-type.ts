import { validateProps } from '../../../../../util';

export enum InternalFabricType {
  ABACA = 'ABACA',
  ALFA = 'ALFA',
  BAMBOO = 'BAMBOO',
  HEMP = 'HEMP',
  COTTON = 'COTTON',
  COCONUT = 'COCONUT',
  CASHMERE = 'CASHMERE',
  HENEQUEN = 'HENEQUEN',
  HALF_LINEN = 'HALF_LINEN',
  JUTE = 'JUTE',
  KENAF = 'KENAF',
  KAPOK = 'KAPOK',
  LINEN = 'LINEN',
  MAGUEY = 'MAGUEY',
  RAMIE = 'RAMIE',
  SISAL = 'SISAL',
  SUNN = 'SUNN',
  CELLULOSE_ACETATE = 'CELLULOSE_ACETATE',
  CUPRO = 'CUPRO',
  LYOCELL = 'LYOCELL',
  MODAL = 'MODAL',
  PAPER = 'PAPER',
  TRIACETATE = 'TRIACETATE',
  VISCOSE = 'VISCOSE',
  ARAMID = 'ARAMID',
  CARBON_FIBER = 'CARBON_FIBER',
  CHLORO_FIBER = 'CHLORO_FIBER',
  ELASTANE = 'ELASTANE',
  FLUOR_FIBER = 'FLUOR_FIBER',
  LUREX = 'LUREX',
  MODACRYLIC = 'MODACRYLIC',
  NYLON = 'NYLON',
  POLYAMIDE = 'POLYAMIDE',
  POLYCARBAMIDE = 'POLYCARBAMIDE',
  ACRYLIC = 'ACRYLIC',
  POLYETHYLENE = 'POLYETHYLENE',
  POLYESTER = 'POLYESTER',
  POLYPROPYLENE = 'POLYPROPYLENE',
  POLYURETHANE = 'POLYURETHANE',
  POLYVINYL_CHLORIDE = 'POLYVINYL_CHLORIDE',
  TETORON_COTTON = 'TETORON_COTTON',
  TRIVINYL = 'TRIVINYL',
  VINYL = 'VINYL',
  HAIR = 'HAIR',
  COW_HAIR = 'COW_HAIR',
  HORSE_HAIR = 'HORSE_HAIR',
  GOAT_HAIR = 'GOAT_HAIR',
  SILK = 'SILK',
  ANGORA_WOOL = 'ANGORA_WOOL',
  BEAVER = 'BEAVER',
  CASHGORA_GOAT = 'CASHGORA_GOAT',
  CAMEL = 'CAMEL',
  LAMA = 'LAMA',
  ANGORA_GOAT = 'ANGORA_GOAT',
  WOOL = 'WOOL',
  ALPAKA = 'ALPAKA',
  OTTER = 'OTTER',
  VIRGIN_WOOL = 'VIRGIN_WOOL',
  YAK = 'YAK',
  UNKNOWN = 'UNKNOWN',
}

export class FabricType {
  readonly internal: InternalFabricType;
  readonly label: string;

  private constructor(props: { internal: InternalFabricType; label: string }) {
    validateProps(props);

    this.internal = props.internal;
    this.label = props.label;
  }

  static cotton(): FabricType {
    return new FabricType({ internal: InternalFabricType.COTTON, label: 'Baumwolle' });
  }

  static cashmere(): FabricType {
    return new FabricType({ internal: InternalFabricType.CASHMERE, label: 'Kaschmir' });
  }

  static abaca(): FabricType {
    return new FabricType({ internal: InternalFabricType.ABACA, label: 'Abaca' });
  }

  static alfa(): FabricType {
    return new FabricType({ internal: InternalFabricType.ALFA, label: 'Alfa' });
  }

  static bambus(): FabricType {
    return new FabricType({ internal: InternalFabricType.BAMBOO, label: 'Bambus' });
  }

  static hemp(): FabricType {
    return new FabricType({ internal: InternalFabricType.HEMP, label: 'Hanf' });
  }

  static coconut(): FabricType {
    return new FabricType({ internal: InternalFabricType.COCONUT, label: 'Kokos' });
  }

  static henequen(): FabricType {
    return new FabricType({ internal: InternalFabricType.HENEQUEN, label: 'Henequen' });
  }

  static halfLinen(): FabricType {
    return new FabricType({ internal: InternalFabricType.HALF_LINEN, label: 'Halbleinen' });
  }

  static jute(): FabricType {
    return new FabricType({ internal: InternalFabricType.JUTE, label: 'Jute' });
  }

  static kenaf(): FabricType {
    return new FabricType({ internal: InternalFabricType.KENAF, label: 'Kenaf' });
  }

  static kapok(): FabricType {
    return new FabricType({ internal: InternalFabricType.KAPOK, label: 'Kapok' });
  }

  static linen(): FabricType {
    return new FabricType({ internal: InternalFabricType.LINEN, label: 'Leinen' });
  }

  static maguey(): FabricType {
    return new FabricType({ internal: InternalFabricType.MAGUEY, label: 'Maguey' });
  }

  static ramie(): FabricType {
    return new FabricType({ internal: InternalFabricType.RAMIE, label: 'Ramie' });
  }

  static sisal(): FabricType {
    return new FabricType({ internal: InternalFabricType.SISAL, label: 'Sisal' });
  }

  static sunn(): FabricType {
    return new FabricType({ internal: InternalFabricType.SUNN, label: 'Sunn' });
  }

  static celluloseAcetate(): FabricType {
    return new FabricType({ internal: InternalFabricType.CELLULOSE_ACETATE, label: 'Celluloseacetat' });
  }

  static cupro(): FabricType {
    return new FabricType({ internal: InternalFabricType.CUPRO, label: 'Cupro' });
  }

  static lyocell(): FabricType {
    return new FabricType({ internal: InternalFabricType.LYOCELL, label: 'Lyocell' });
  }

  static modal(): FabricType {
    return new FabricType({ internal: InternalFabricType.MODAL, label: 'Modal' });
  }

  static paper(): FabricType {
    return new FabricType({ internal: InternalFabricType.PAPER, label: 'Papiergarn' });
  }

  static triacetate(): FabricType {
    return new FabricType({ internal: InternalFabricType.TRIACETATE, label: 'Triacetat' });
  }

  static viscose(): FabricType {
    return new FabricType({ internal: InternalFabricType.VISCOSE, label: 'Viskose' });
  }

  static aramid(): FabricType {
    return new FabricType({ internal: InternalFabricType.ARAMID, label: 'Aramid' });
  }

  static carbonFiber(): FabricType {
    return new FabricType({ internal: InternalFabricType.CARBON_FIBER, label: 'Kohlefaser' });
  }

  static chloroFiber(): FabricType {
    return new FabricType({ internal: InternalFabricType.CHLORO_FIBER, label: 'Chlorofaser' });
  }

  static elastane(): FabricType {
    return new FabricType({ internal: InternalFabricType.ELASTANE, label: 'Elastan' });
  }

  static fluorFiber(): FabricType {
    return new FabricType({ internal: InternalFabricType.FLUOR_FIBER, label: 'Fluorfaser' });
  }

  static lurex(): FabricType {
    return new FabricType({ internal: InternalFabricType.LUREX, label: 'Lurex' });
  }

  static modacrylic(): FabricType {
    return new FabricType({ internal: InternalFabricType.MODACRYLIC, label: 'Modacryl' });
  }

  static nylon(): FabricType {
    return new FabricType({ internal: InternalFabricType.NYLON, label: 'Nylon' });
  }

  static polyamide(): FabricType {
    return new FabricType({ internal: InternalFabricType.POLYAMIDE, label: 'Polyamid' });
  }

  static polycarbamide(): FabricType {
    return new FabricType({ internal: InternalFabricType.POLYCARBAMIDE, label: 'Polycarbamid' });
  }

  static acrylic(): FabricType {
    return new FabricType({ internal: InternalFabricType.ACRYLIC, label: 'Acryl' });
  }

  static polyethylene(): FabricType {
    return new FabricType({ internal: InternalFabricType.POLYETHYLENE, label: 'Polyethylen' });
  }

  static polyester(): FabricType {
    return new FabricType({ internal: InternalFabricType.POLYESTER, label: 'Polyester' });
  }

  static polypropylene(): FabricType {
    return new FabricType({ internal: InternalFabricType.POLYPROPYLENE, label: 'Polypropylen' });
  }

  static polyurethane(): FabricType {
    return new FabricType({ internal: InternalFabricType.POLYURETHANE, label: 'Polyurethan' });
  }

  static polyvinylChloride(): FabricType {
    return new FabricType({ internal: InternalFabricType.POLYVINYL_CHLORIDE, label: 'Polyvinylchlorid' });
  }

  static tetoronCotton(): FabricType {
    return new FabricType({ internal: InternalFabricType.TETORON_COTTON, label: 'Tetoron-Baumwolle' });
  }

  static trivinyl(): FabricType {
    return new FabricType({ internal: InternalFabricType.TRIVINYL, label: 'Trivinyl' });
  }

  static vinyl(): FabricType {
    return new FabricType({ internal: InternalFabricType.VINYL, label: 'Vinyl' });
  }

  static hair(): FabricType {
    return new FabricType({ internal: InternalFabricType.HAIR, label: 'Haar' });
  }

  static cowHair(): FabricType {
    return new FabricType({ internal: InternalFabricType.COW_HAIR, label: 'Rinderhaar' });
  }

  static horseHair(): FabricType {
    return new FabricType({ internal: InternalFabricType.HORSE_HAIR, label: 'Pferdehaar' });
  }

  static goatHair(): FabricType {
    return new FabricType({ internal: InternalFabricType.GOAT_HAIR, label: 'Ziegenhaar' });
  }

  static silk(): FabricType {
    return new FabricType({ internal: InternalFabricType.SILK, label: 'Seide' });
  }

  static angoraWool(): FabricType {
    return new FabricType({ internal: InternalFabricType.ANGORA_WOOL, label: 'Angorawolle' });
  }

  static beaver(): FabricType {
    return new FabricType({ internal: InternalFabricType.BEAVER, label: 'Biber' });
  }

  static cashgoraGoat(): FabricType {
    return new FabricType({ internal: InternalFabricType.CASHGORA_GOAT, label: 'Cashgora-Ziege' });
  }

  static camel(): FabricType {
    return new FabricType({ internal: InternalFabricType.CAMEL, label: 'Kamel' });
  }

  static lama(): FabricType {
    return new FabricType({ internal: InternalFabricType.LAMA, label: 'Lama' });
  }

  static angoraGoat(): FabricType {
    return new FabricType({ internal: InternalFabricType.ANGORA_GOAT, label: 'Angora-Ziege' });
  }

  static wool(): FabricType {
    return new FabricType({ internal: InternalFabricType.WOOL, label: 'Wolle' });
  }

  static alpaka(): FabricType {
    return new FabricType({ internal: InternalFabricType.ALPAKA, label: 'Alpaka' });
  }

  static otter(): FabricType {
    return new FabricType({ internal: InternalFabricType.OTTER, label: 'Otter' });
  }

  static virginWool(): FabricType {
    return new FabricType({ internal: InternalFabricType.VIRGIN_WOOL, label: 'Schurwolle' });
  }

  static yak(): FabricType {
    return new FabricType({ internal: InternalFabricType.YAK, label: 'Yak' });
  }

  static unknown(): FabricType {
    return new FabricType({ internal: InternalFabricType.UNKNOWN, label: 'Unbekannt' });
  }
}

export const ABACA = FabricType.abaca();
export const ALFA = FabricType.alfa();
export const BAMBOO = FabricType.bambus();
export const HEMP = FabricType.hemp();
export const COTTON = FabricType.cotton();
export const CASHMERE = FabricType.cashmere();
export const COCONUT = FabricType.coconut();
export const HENEQUEN = FabricType.henequen();
export const HALF_LINEN = FabricType.halfLinen();
export const JUTE = FabricType.jute();
export const KENAF = FabricType.kenaf();
export const KAPOK = FabricType.kapok();
export const LINEN = FabricType.linen();
export const MAGUEY = FabricType.maguey();
export const RAMIE = FabricType.ramie();
export const SISAL = FabricType.sisal();
export const SUNN = FabricType.sunn();
export const CELLULOSE_ACETATE = FabricType.celluloseAcetate();
export const CUPRO = FabricType.cupro();
export const LYOCELL = FabricType.lyocell();
export const MODAL = FabricType.modal();
export const PAPER = FabricType.paper();
export const TRIACETATE = FabricType.triacetate();
export const VISCOSE = FabricType.viscose();
export const ARAMID = FabricType.aramid();
export const CARBON_FIBER = FabricType.carbonFiber();
export const CHLORO_FIBER = FabricType.chloroFiber();
export const ELASTANE = FabricType.elastane();
export const FLUOR_FIBER = FabricType.fluorFiber();
export const LUREX = FabricType.lurex();
export const MODACRYLIC = FabricType.modacrylic();
export const NYLON = FabricType.nylon();
export const POLYAMIDE = FabricType.polyamide();
export const POLYCARBAMIDE = FabricType.polycarbamide();
export const ACRYLIC = FabricType.acrylic();
export const POLYETHYLENE = FabricType.polyethylene();
export const POLYESTER = FabricType.polyester();
export const POLYPROPYLENE = FabricType.polypropylene();
export const POLYURETHANE = FabricType.polyurethane();
export const POLYVINYL_CHLORIDE = FabricType.polyvinylChloride();
export const TETORON_COTTON = FabricType.tetoronCotton();
export const TRIVINYL = FabricType.trivinyl();
export const VINYL = FabricType.vinyl();
export const HAIR = FabricType.hair();
export const COW_HAIR = FabricType.cowHair();
export const HORSE_HAIR = FabricType.horseHair();
export const GOAT_HAIR = FabricType.goatHair();
export const SILK = FabricType.silk();
export const ANGORA_WOOL = FabricType.angoraWool();
export const BEAVER = FabricType.beaver();
export const CASHGORA_GOAT = FabricType.cashgoraGoat();
export const CAMEL = FabricType.camel();
export const LAMA = FabricType.lama();
export const ANGORA_GOAT = FabricType.angoraGoat();
export const WOOL = FabricType.wool();
export const ALPAKA = FabricType.alpaka();
export const OTTER = FabricType.otter();
export const VIRGIN_WOOL = FabricType.virginWool();
export const YAK = FabricType.yak();
export const UNKNOWN = FabricType.unknown();

export const FABRIC_TYPES = [
  ABACA,
  ALFA,
  BAMBOO,
  HEMP,
  COTTON,
  CASHMERE,
  COCONUT,
  HENEQUEN,
  HALF_LINEN,
  JUTE,
  KENAF,
  KAPOK,
  LINEN,
  MAGUEY,
  RAMIE,
  SISAL,
  SUNN,
  CELLULOSE_ACETATE,
  CUPRO,
  LYOCELL,
  MODAL,
  PAPER,
  TRIACETATE,
  VISCOSE,
  ARAMID,
  CARBON_FIBER,
  CHLORO_FIBER,
  ELASTANE,
  FLUOR_FIBER,
  LUREX,
  MODACRYLIC,
  NYLON,
  POLYAMIDE,
  POLYCARBAMIDE,
  ACRYLIC,
  POLYETHYLENE,
  POLYESTER,
  POLYPROPYLENE,
  POLYURETHANE,
  POLYVINYL_CHLORIDE,
  TETORON_COTTON,
  TRIVINYL,
  VINYL,
  HAIR,
  COW_HAIR,
  HORSE_HAIR,
  GOAT_HAIR,
  SILK,
  ANGORA_WOOL,
  BEAVER,
  CASHGORA_GOAT,
  CAMEL,
  LAMA,
  ANGORA_GOAT,
  WOOL,
  ALPAKA,
  OTTER,
  VIRGIN_WOOL,
  YAK,
  UNKNOWN,
];
