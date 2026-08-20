import { useState } from 'react'
import { Link } from 'react-router-dom'
import Button from '../../components/Button'
import { Field, FieldGrid } from '../../components/Field'
import HairlineSection from '../../components/HairlineSection'
import MrzBar from '../../components/MrzBar'
import Stamp from '../../components/Stamp'
import GuillocheBg from '../../components/GuillocheBg'
import StockStatus from '../../components/StockStatus'
import Toast from '../../components/Toast'
import Loading from '../../components/Loading'
import ProgressGauge from '../../components/ProgressGauge'
import CharacterBubble from '../../components/CharacterBubble'

// 전 공통 컴포넌트를 상태별로 나열하는 개발용 화면. 배포 산출물에는 링크를 노출하지 않는다.
export default function KitchenSink() {
  const [mrzToggle, setMrzToggle] = useState(false)
  const [showToast, setShowToast] = useState(false)

  return (
    <div className="theme-light mx-auto max-w-[402px] bg-bone-050 px-5 pb-24 pt-8">
      <Link to="/" className="t-label underline underline-offset-4" style={{ color: 'var(--graphite)' }}>
        메뉴로
      </Link>
      <h1 className="t-display-l mb-6 mt-2">Kitchen Sink</h1>

      <HairlineSection title="Buttons">
        <div className="flex flex-col gap-3">
          <Button variant="primary">패스포트에 저장</Button>
          <Button variant="secondary">직원 호출</Button>
          <Button variant="text">대화 이어가기</Button>
          <Button variant="primary" disabled>
            비활성
          </Button>
        </div>
      </HairlineSection>

      <HairlineSection title="Field / FieldGrid">
        <div className="flex flex-col gap-4">
          <Field label="SIZE" value="26cm / 260" />
          <FieldGrid
            fields={[
              { label: 'MATERIAL', value: '코티드 캔버스' },
              { label: 'WEIGHT', value: '780g' },
              { label: 'SIZE', value: '26cm (260)' },
            ]}
          />
        </div>
      </HairlineSection>

      <HairlineSection title="StockStatus">
        <div className="flex flex-col gap-3">
          <StockStatus status="IN_STOCK" label="이 매장" caption="재고 있음" />
          <StockStatus status="TRANSFERABLE" label="현대 무역센터" caption="이동 가능" />
          <StockStatus status="ONLINE_ONLY" label="온라인 스토어" caption="온라인 주문 가능" />
          <StockStatus status="OUT_OF_STOCK" label="코즈웨이베이" caption="재고 없음" />
        </div>
      </HairlineSection>

      <HairlineSection title="Stamp">
        <div className="flex gap-6">
          <Stamp label="ZONE01" rotationSeed={12} />
          <Stamp label="ZONE03" rotationSeed={77} />
        </div>
      </HairlineSection>

      <HairlineSection title="GuillocheBg">
        <div className="relative h-24 overflow-hidden" style={{ background: 'var(--bone-100)' }}>
          <GuillocheBg />
        </div>
      </HairlineSection>

      <HairlineSection title="CharacterBubble">
        <CharacterBubble name="카이저" message="스카이 스트림, 세 번째 보시네요. 컬러 때문인가요, 사이즈 때문인가요?" />
      </HairlineSection>

      <HairlineSection title="ProgressGauge">
        <ProgressGauge progress={0.6} />
      </HairlineSection>

      <HairlineSection title="Loading">
        <Loading />
      </HairlineSection>

      <HairlineSection title="Toast">
        <Button variant="secondary" onClick={() => setShowToast(true)}>
          토스트 띄우기
        </Button>
        {showToast && (
          <Toast lines={['패스포트에 저장했습니다', 'SAVED07']} onDismiss={() => setShowToast(false)} />
        )}
      </HairlineSection>

      <HairlineSection title="MrzBar">
        <Button variant="secondary" onClick={() => setMrzToggle((v) => !v)}>
          MRZ 값 전환
        </Button>
      </HairlineSection>

      <MrzBar
        lines={
          mrzToggle
            ? ['ENTRY<<SEOUL<SEONGSU<<ZONE03<<<<<<<<<<<<<<', 'SAVED07<<INTENT<COLOR<<MKT<HK<<<<<<<<<<']
            : ['ENTRY<<SEOUL<SEONGSU<<ZONE03<<<<<<<<<<<<<<', 'SAVED07<<INTENT<COLOR<<MKT<KR<<<<<<<<<<']
        }
        accessibleLabel="성수 팝업 3구역. 저장 7건. 미해결 요인 컬러. 현재 시장 한국."
      />
    </div>
  )
}
